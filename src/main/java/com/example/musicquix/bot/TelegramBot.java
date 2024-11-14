package com.example.musicquix.bot;

import com.example.musicquix.dto.SongDTO;
import com.example.musicquix.service.MusicService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;

    private final MusicService service;

    private final Button button;

    Language language = Language.ENGLISH;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/game":
                    game(chatId, service.songLyrics(language));
                case "/only_rus":
            if (language.equals(Language.ENGLISH)) {
                language = Language.RUSSIAN;
            }

            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("1")) {
                String text = "Правильный ответ!";
                executeEditMessageText(text, chatId, messageId);
            } else if (!(callbackData.equals("1"))) {
                String band = "";
                for (List<InlineKeyboardButton> list : update.getCallbackQuery().getMessage().getReplyMarkup().getKeyboard()) {
                    for (InlineKeyboardButton l2 : list) {
                        if (l2.getCallbackData().equals("1")) {
                            band = l2.getText();
                        }
                    }
                }
                String text = "Ответ неверный :(" + " Правильный ответ: " + band;
                executeEditMessageText(text, chatId, messageId);
            }
        }
    }


    private void executeEditMessageText(String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Здарова, " + name + "\n" +
                "Это квиз игра в которой надо угадать исполнителя по тексту песни." + "\n" +
                "Для начала игры пропиши /game " + "\n" +
                "Удачи!";
        sendMessage(chatId, answer);
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(button.remove());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            // e.printStackTrace();
        }
    }

    private void sendMessageGame(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(button.replyKeyboardMarkup());
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            // e.printStackTrace();
        }
    }

    private void game(long chatId, SongDTO songDTO) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(songDTO.getSongBand().keySet().toString());

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();

        var button1 = new InlineKeyboardButton();

        String text = "";

        for (String s : songDTO.getSongBand().values()) {
            text = s + text;
        }

        button1.setText(text);
        button1.setCallbackData("1");

        var button2 = new InlineKeyboardButton();

        button2.setText(songDTO.getBandNames().get(0));
        button2.setCallbackData("2");

        var button3 = new InlineKeyboardButton();

        button3.setText(songDTO.getBandNames().get(1));
        button3.setCallbackData("3");

        var button4 = new InlineKeyboardButton();

        button4.setText(songDTO.getBandNames().get(2));
        button4.setCallbackData("4");


        rowInLine1.add(button1);
        rowInLine1.add(button2);

        Collections.shuffle(rowInLine1);

        rowInLine2.add(button3);
        rowInLine2.add(button4);

        Collections.shuffle(rowInLine2);


        rowsInLine.add(rowInLine1);
        rowsInLine.add(rowInLine2);

        Collections.shuffle(rowsInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);


    }


    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
