package com.example.musicquix.bot;

import com.example.musicquix.dto.QuizQuestion;
import com.example.musicquix.service.MusicService;
import com.example.musicquix.service.NoQuestionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    // callback data formats (Telegram limits it to 64 bytes)
    private static final String CB_ANSWER = "a:";   // a:<chosenIndex>:<correctIndex>
    private static final String CB_NEXT = "next";
    private static final String CB_LANG = "lang:";  // lang:<Language name>

    private static final class Score {
        final AtomicInteger correct = new AtomicInteger();
        final AtomicInteger total = new AtomicInteger();
    }

    private final BotConfig botConfig;
    private final MusicService service;

    // In-memory per-chat state: lost on restart, which is acceptable for a quiz.
    private final Map<Long, Language> languages = new ConcurrentHashMap<>();
    private final Map<Long, Score> scores = new ConcurrentHashMap<>();

    public TelegramBot(BotConfig botConfig, MusicService service) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.service = service;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    public void registerCommands() throws TelegramApiException {
        execute(SetMyCommands.builder()
                .command(new BotCommand("game", "Новый вопрос"))
                .command(new BotCommand("lang", "Выбрать язык песен"))
                .command(new BotCommand("stats", "Моя статистика"))
                .command(new BotCommand("help", "Как играть"))
                .scope(new BotCommandScopeDefault())
                .build());
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                handleCallback(update.getCallbackQuery());
            } else if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to handle update {}", update.getUpdateId(), e);
        }
    }

    // ---------------------------------------------------------------- messages

    private void handleMessage(Message message) throws TelegramApiException {
        long chatId = message.getChatId();
        // "/game@MQIZ_BOT arg" -> "/game"
        String command = message.getText().trim().split("[\\s@]", 2)[0].toLowerCase();

        switch (command) {
            case "/start" -> sendText(chatId, "Привет, " + message.getFrom().getFirstName() + "!\n"
                    + "Это квиз: нужно угадать исполнителя по отрывку текста песни.\n"
                    + "/game — новый вопрос\n/lang — язык песен\n/stats — статистика\nУдачи!");
            case "/help" -> sendText(chatId, "Я показываю 4 строки из песни, ты выбираешь исполнителя из "
                    + "четырёх вариантов.\n/game — новый вопрос\n/lang — язык песен\n/stats — статистика");
            case "/game" -> sendQuestion(chatId);
            case "/lang" -> sendLanguageChoice(chatId);
            case "/only_rus" -> setLanguage(chatId, Language.RUSSIAN); // backward compatibility
            case "/stats" -> sendStats(chatId);
            default -> { /* ignore other text */ }
        }
    }

    private void sendQuestion(long chatId) throws TelegramApiException {
        QuizQuestion q;
        try {
            q = service.newQuestion(languages.getOrDefault(chatId, Language.ENGLISH));
        } catch (NoQuestionException e) {
            log.warn("No question available: {}", e.getMessage());
            sendText(chatId, "Не удалось подобрать вопрос. Попробуй ещё раз чуть позже.");
            return;
        }
        SendMessage message = new SendMessage(String.valueOf(chatId), "🎵 Угадай исполнителя:\n\n" + q.lyrics());
        message.setReplyMarkup(answerKeyboard(q));
        execute(message);
    }

    private InlineKeyboardMarkup answerKeyboard(QuizQuestion q) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < q.options().size(); i++) {
            row.add(button(q.options().get(i), CB_ANSWER + i + ":" + q.correctIndex()));
            if (row.size() == 2) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        return new InlineKeyboardMarkup(rows);
    }

    private void sendLanguageChoice(long chatId) throws TelegramApiException {
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (Language l : Language.values()) {
            row.add(button(l.label(), CB_LANG + l.name()));
        }
        SendMessage message = new SendMessage(String.valueOf(chatId), "Выбери язык песен:");
        message.setReplyMarkup(new InlineKeyboardMarkup(List.of(row)));
        execute(message);
    }

    private void setLanguage(long chatId, Language language) throws TelegramApiException {
        languages.put(chatId, language);
        sendText(chatId, "Язык песен: " + language.label() + ". Жми /game!");
    }

    private void sendStats(long chatId) throws TelegramApiException {
        Score s = scores.get(chatId);
        if (s == null || s.total.get() == 0) {
            sendText(chatId, "Пока нет ответов. Начни с /game!");
            return;
        }
        int total = s.total.get();
        int correct = s.correct.get();
        sendText(chatId, "Правильных ответов: " + correct + " из " + total
                + " (" + Math.round(100.0 * correct / total) + "%)");
    }

    // --------------------------------------------------------------- callbacks

    private void handleCallback(CallbackQuery callback) throws TelegramApiException {
        String data = callback.getData();
        Message message = callback.getMessage();
        if (data == null || message == null) {
            return;
        }
        long chatId = message.getChatId();
        // always acknowledge so the client stops showing the loading spinner
        execute(AnswerCallbackQuery.builder().callbackQueryId(callback.getId()).build());

        if (data.equals(CB_NEXT)) {
            sendQuestion(chatId);
        } else if (data.startsWith(CB_LANG)) {
            try {
                setLanguage(chatId, Language.valueOf(data.substring(CB_LANG.length())));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown language callback: {}", data);
            }
        } else if (data.startsWith(CB_ANSWER)) {
            handleAnswer(chatId, message, data.substring(CB_ANSWER.length()));
        }
    }

    private void handleAnswer(long chatId, Message message, String payload) throws TelegramApiException {
        String[] parts = payload.split(":");
        int chosen;
        int correct;
        try {
            chosen = Integer.parseInt(parts[0]);
            correct = Integer.parseInt(parts[1]);
        } catch (RuntimeException e) {
            log.warn("Malformed answer callback: {}", payload);
            return;
        }
        // The question message was already answered (its keyboard was replaced): ignore stale taps.
        List<InlineKeyboardButton> buttons = message.getReplyMarkup() == null ? List.of()
                : message.getReplyMarkup().getKeyboard().stream().flatMap(List::stream).toList();
        if (correct < 0 || correct >= buttons.size()
                || buttons.stream().noneMatch(b -> b.getCallbackData().startsWith(CB_ANSWER))) {
            return;
        }

        Score score = scores.computeIfAbsent(chatId, id -> new Score());
        score.total.incrementAndGet();
        String result;
        if (chosen == correct) {
            score.correct.incrementAndGet();
            result = "✅ Правильно! Это " + buttons.get(correct).getText();
        } else {
            result = "❌ Неверно. Правильный ответ: " + buttons.get(correct).getText();
        }

        EditMessageText edit = new EditMessageText(message.getText() + "\n\n" + result);
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(message.getMessageId());
        edit.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(button("Следующая песня ▶", CB_NEXT)))));
        execute(edit);
    }

    // ----------------------------------------------------------------- helpers

    private void sendText(long chatId, String text) throws TelegramApiException {
        execute(new SendMessage(String.valueOf(chatId), text));
    }

    private static InlineKeyboardButton button(String text, String callbackData) {
        InlineKeyboardButton b = new InlineKeyboardButton(text);
        b.setCallbackData(callbackData);
        return b;
    }
}
