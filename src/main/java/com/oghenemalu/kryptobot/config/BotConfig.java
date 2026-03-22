package com.oghenemalu.kryptobot.config;

import com.oghenemalu.kryptobot.bot.MyTelegramBot;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotConfig implements ApplicationListener<ApplicationReadyEvent> {

    private final MyTelegramBot bot;

    public BotConfig(MyTelegramBot bot) {
        this.bot = bot;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(bot);
            System.out.println("✅ Bot registered and polling started!");
        } catch (TelegramApiException e) {
            throw new RuntimeException("Failed to register Telegram bot", e);
        }
    }
}