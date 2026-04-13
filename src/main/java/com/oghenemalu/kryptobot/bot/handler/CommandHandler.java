package com.oghenemalu.kryptobot.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface CommandHandler {
    boolean canHandle(String command);
    void handle(Update update, AbsSender absSender);
}
