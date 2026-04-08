package com.oghenemalu.kryptobot.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {
    boolean canHandle(String command);
    void handle(Update update);
}
