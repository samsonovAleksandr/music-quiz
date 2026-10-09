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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    // callback data formats (Telegram limits it to 64 bytes)
    private static final String CB_ANSWER = "a:";        // a:<chosenIndex>:<correctIndex>
    private static final String CB_NEXT = "next";
    private static final String CB_LANG = "lang:";       // lang:<Language name>
    private static final String CB_LANG_MENU = "langmenu";
    private static final String CB_STATS = "stats";

    private static final String OPTION_SEP = " · ";
    private static final int ROUND_CACHE_SIZE = 5000;

    private static final class Score {
        int correct;
        int total;
        int streak;
        int best;

        synchronized int streak() {
            return streak;
        }

        synchronized void record(boolean right) {
            total++;
            if (right) {
                correct++;
                streak++;
                best = Math.max(best, streak);
            } else {
                streak = 0;
            }
        }
    }

    /** What we remember about a question message so the answer can be rendered nicely. */
    private record Round(Language language, String lyrics, String title) {
    }

    private final BotConfig botConfig;
    private final MusicService service;

    // In-memory per-chat state: lost on restart, which is acceptable for a quiz.
    private final Map<Long, Language> languages = new ConcurrentHashMap<>();
    private final Map<Long, Score> scores = new ConcurrentHashMap<>();
    private final Map<String, Round> rounds = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Round> eldest) {
            return size() > ROUND_CACHE_SIZE;
        }
    };

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
                .command(new BotCommand("game", "🎮 Новый вопрос"))
                .command(new BotCommand("lang", "🌐 Язык песен"))
                .command(new BotCommand("stats", "📊 Моя статистика"))
                .command(new BotCommand("help", "ℹ️ Как играть"))
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
            case "/start" -> sendHtml(chatId, Messages.welcome(message.getFrom().getFirstName()),
                    keyboard(List.of(button("🎮 Играть", CB_NEXT), button("🌐 Язык", CB_LANG_MENU))));
            case "/help" -> sendHtml(chatId, Messages.help(), keyboard(List.of(button("🎮 Играть", CB_NEXT))));
            case "/game" -> sendQuestion(chatId);
            case "/lang" -> sendLanguageChoice(chatId);
            case "/only_rus" -> chooseLanguage(chatId, null, Language.RUSSIAN); // backward compatibility
            case "/stats" -> sendStats(chatId);
            default -> { /* ignore other text */ }
        }
    }

    private void sendQuestion(long chatId) throws TelegramApiException {
        Language language = languages.getOrDefault(chatId, Language.ENGLISH);
        QuizQuestion q;
        try {
            q = service.newQuestion(language);
        } catch (NoQuestionException e) {
            log.warn("No question available: {}", e.getMessage());
            sendHtml(chatId, Messages.noQuestion(), keyboard(List.of(button("🔄 Ещё раз", CB_NEXT))));
            return;
        }
        int streak = score(chatId).streak();
        SendMessage message = new SendMessage(String.valueOf(chatId), Messages.question(language, q.lyrics(), streak));
        message.setParseMode("HTML");
        message.setReplyMarkup(answerKeyboard(q));
        Message sent = execute(message);
        synchronized (rounds) {
            rounds.put(roundKey(chatId, sent.getMessageId()), new Round(language, q.lyrics(), q.title()));
        }
    }

    private InlineKeyboardMarkup answerKeyboard(QuizQuestion q) {
        // one option per row: artist names can be long and are easier to read and tap this way
        String[] letters = {"A", "B", "C", "D"};
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < q.options().size(); i++) {
            String label = (i < letters.length ? letters[i] : String.valueOf(i + 1)) + OPTION_SEP + q.options().get(i);
            rows.add(List.of(button(label, CB_ANSWER + i + ":" + q.correctIndex())));
        }
        return new InlineKeyboardMarkup(rows);
    }

    private void sendLanguageChoice(long chatId) throws TelegramApiException {
        sendHtml(chatId, Messages.languagePrompt(), languageKeyboard(chatId));
    }

    private InlineKeyboardMarkup languageKeyboard(long chatId) {
        Language current = languages.getOrDefault(chatId, Language.ENGLISH);
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (Language l : Language.values()) {
            String mark = l == current ? "✅ " : "";
            row.add(button(mark + Messages.flag(l) + " " + l.label(), CB_LANG + l.name()));
        }
        return keyboard(row);
    }

    /** @param menu the language menu message to replace with a confirmation, or null to send a new message */
    private void chooseLanguage(long chatId, Message menu, Language language) throws TelegramApiException {
        languages.put(chatId, language);
        InlineKeyboardMarkup play = keyboard(List.of(button("🎮 Играть", CB_NEXT)));
        if (menu != null) {
            editHtml(chatId, menu.getMessageId(), Messages.languageChosen(language), play);
        } else {
            sendHtml(chatId, Messages.languageChosen(language), play);
        }
    }

    private void sendStats(long chatId) throws TelegramApiException {
        Score s = scores.get(chatId);
        String text = s == null ? Messages.stats(0, 0, 0, 0)
                : Messages.stats(s.correct, s.total, s.streak, s.best);
        sendHtml(chatId, text, keyboard(List.of(button("🎮 Играть", CB_NEXT))));
    }

    // --------------------------------------------------------------- callbacks

    private void handleCallback(CallbackQuery callback) throws TelegramApiException {
        String data = callback.getData();
        Message message = callback.getMessage();
        if (data == null || message == null) {
            return;
        }
        long chatId = message.getChatId();

        if (data.startsWith(CB_ANSWER)) {
            handleAnswer(callback, chatId, message, data.substring(CB_ANSWER.length()));
            return;
        }
        // always acknowledge so the client stops showing the loading spinner
        execute(AnswerCallbackQuery.builder().callbackQueryId(callback.getId()).build());

        if (data.equals(CB_NEXT)) {
            sendQuestion(chatId);
        } else if (data.equals(CB_LANG_MENU)) {
            sendLanguageChoice(chatId);
        } else if (data.equals(CB_STATS)) {
            sendStats(chatId);
        } else if (data.startsWith(CB_LANG)) {
            try {
                chooseLanguage(chatId, message, Language.valueOf(data.substring(CB_LANG.length())));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown language callback: {}", data);
            }
        }
    }

    private void handleAnswer(CallbackQuery callback, long chatId, Message message, String payload)
            throws TelegramApiException {
        String[] parts = payload.split(":");
        int chosen;
        int correct;
        try {
            chosen = Integer.parseInt(parts[0]);
            correct = Integer.parseInt(parts[1]);
        } catch (RuntimeException e) {
            log.warn("Malformed answer callback: {}", payload);
            execute(AnswerCallbackQuery.builder().callbackQueryId(callback.getId()).build());
            return;
        }
        // Option names are read back from the keyboard, so the callback stays tiny and stateless.
        List<String> options = new ArrayList<>();
        if (message.getReplyMarkup() != null) {
            for (List<InlineKeyboardButton> row : message.getReplyMarkup().getKeyboard()) {
                for (InlineKeyboardButton b : row) {
                    if (b.getCallbackData() != null && b.getCallbackData().startsWith(CB_ANSWER)) {
                        options.add(b.getText().replaceFirst("^\\S+" + OPTION_SEP, "")); // drop the "A · " prefix
                    }
                }
            }
        }
        // The message was already answered (its keyboard was replaced): ignore stale taps.
        if (options.isEmpty() || correct < 0 || correct >= options.size() || chosen < 0 || chosen >= options.size()) {
            execute(AnswerCallbackQuery.builder().callbackQueryId(callback.getId()).build());
            return;
        }

        Score score = score(chatId);
        score.record(chosen == correct);
        execute(AnswerCallbackQuery.builder().callbackQueryId(callback.getId())
                .text(chosen == correct ? "✅ Верно!" : "❌ Это " + options.get(correct)).build());

        Round round;
        synchronized (rounds) {
            round = rounds.remove(roundKey(chatId, message.getMessageId()));
        }
        Language language = round != null ? round.language() : languages.getOrDefault(chatId, Language.ENGLISH);
        String lyrics = round != null ? round.lyrics() : lyricsFromMessage(message.getText());
        String title = round != null ? round.title() : null;

        int streak;
        int best;
        synchronized (score) {
            streak = score.streak;
            best = score.best;
        }
        editHtml(chatId, message.getMessageId(),
                Messages.result(language, lyrics, title, options, chosen, correct, streak, best),
                keyboard(List.of(button("▶️ Дальше", CB_NEXT), button("📊 Статистика", CB_STATS))));
    }

    // ----------------------------------------------------------------- helpers

    private Score score(long chatId) {
        return scores.computeIfAbsent(chatId, id -> new Score());
    }

    /** Fallback after a restart: question text is "<header>\n\n<lyrics>". */
    private static String lyricsFromMessage(String text) {
        if (text == null) {
            return "";
        }
        int i = text.indexOf("\n\n");
        return i >= 0 ? text.substring(i + 2) : text;
    }

    private static String roundKey(long chatId, int messageId) {
        return chatId + ":" + messageId;
    }

    private void sendHtml(long chatId, String html, InlineKeyboardMarkup markup) throws TelegramApiException {
        SendMessage message = new SendMessage(String.valueOf(chatId), html);
        message.setParseMode("HTML");
        message.setReplyMarkup(markup);
        execute(message);
    }

    private void editHtml(long chatId, int messageId, String html, InlineKeyboardMarkup markup)
            throws TelegramApiException {
        EditMessageText edit = new EditMessageText(html);
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(messageId);
        edit.setParseMode("HTML");
        edit.setReplyMarkup(markup);
        execute(edit);
    }

    private static InlineKeyboardMarkup keyboard(List<InlineKeyboardButton> row) {
        return new InlineKeyboardMarkup(List.of(row));
    }

    private static InlineKeyboardButton button(String text, String callbackData) {
        InlineKeyboardButton b = new InlineKeyboardButton(text);
        b.setCallbackData(callbackData);
        return b;
    }
}
