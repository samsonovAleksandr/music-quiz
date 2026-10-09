package com.example.musicquix.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotInitializer {
    private final TelegramBot telegramBot;
    private final BotConfig botConfig;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (!botConfig.isConfigured()) {
            log.warn("BOT_TOKEN is not set - the Telegram bot is NOT started");
            return;
        }
        try {
            new TelegramBotsApi(DefaultBotSession.class).registerBot(telegramBot);
            telegramBot.registerCommands();
            log.info("Telegram bot @{} started", botConfig.getBotName());
        } catch (TelegramApiException e) {
            log.error("Failed to register Telegram bot", e);
        }
    }
}
