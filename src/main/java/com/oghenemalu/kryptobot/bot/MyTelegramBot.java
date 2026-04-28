package com.oghenemalu.kryptobot.bot;

import com.oghenemalu.kryptobot.bot.handler.CallBackHandler;
import com.oghenemalu.kryptobot.bot.handler.CommandHandler;
import com.oghenemalu.kryptobot.scheduler.AlertScheduler;
import com.oghenemalu.kryptobot.user.UserService;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


@Component
public class MyTelegramBot extends TelegramLongPollingBot {



    @Value("${telegram.username}")
    private String botUsername;


    private final CallBackHandler callBackHandler;
    private final List<CommandHandler> commandHandlers;
    private final AlertScheduler alertScheduler;


    public MyTelegramBot(@Value("${telegram.bot.token}") String token, CallBackHandler callBackHandler, AlertScheduler alertScheduler, List<CommandHandler> commandHandlers) {
        super(token);


        System.out.println("=".repeat(50));
        System.out.println("🤖 BOT INITIALIZATION");
        System.out.println("=".repeat(50));
        System.out.println("Token loaded: " + (token != null ? "✅ YES (length: " + token.length() + ")" : "❌ NO"));
        System.out.println("Username: " + (botUsername != null ? botUsername : "❌ NOT SET"));
        System.out.println("=".repeat(50));

        this.callBackHandler = callBackHandler;
        this.commandHandlers = commandHandlers;
        this.alertScheduler = alertScheduler;
        this.alertScheduler.setAbsSender(this);

    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();

            commandHandlers.forEach(handler -> {
                if (handler.canHandle(message)) {
                    handler.handle(update, this);
                }
            });

            System.out.println(userId + " " + chatId + " " + message);

        } else if (update.hasCallbackQuery()) {
            callBackHandler.handleCallback(update.getCallbackQuery(), this);
        }
    }
}