package com.example.musicquix.bot;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class BotConfig {
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String token;

    public boolean isConfigured() {
        return token != null && !token.isBlank();
    }
}
