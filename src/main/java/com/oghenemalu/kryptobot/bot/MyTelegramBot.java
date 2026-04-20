package com.oghenemalu.kryptobot.bot;

import com.oghenemalu.kryptobot.bot.handler.CallBackHandler;
import com.oghenemalu.kryptobot.bot.handler.CommandHandler;
import com.oghenemalu.kryptobot.scheduler.AlertScheduler;
import com.oghenemalu.kryptobot.user.UserService;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final Dotenv dotenv = Dotenv.load();

    private final CallBackHandler callBackHandler;
    private final List<CommandHandler> commandHandlers;
    private final AlertScheduler alertScheduler;


    public MyTelegramBot(MenuBuilder menuBuilder, CallBackHandler callBackHandler, AlertScheduler alertScheduler, List<CommandHandler> commandHandlers) {
        super(Dotenv.load().get("TELEGRAM_TOKEN"));

        String token = dotenv.get("TELEGRAM_TOKEN");
        String username = dotenv.get("TELEGRAM_BOT_USERNAME");

        System.out.println("=".repeat(50));
        System.out.println("🤖 BOT INITIALIZATION");
        System.out.println("=".repeat(50));
        System.out.println("Token loaded: " + (token != null ? "✅ YES (length: " + token.length() + ")" : "❌ NO"));
        System.out.println("Username: " + (username != null ? username : "❌ NOT SET"));
        System.out.println("=".repeat(50));

        this.callBackHandler = callBackHandler;
        this.commandHandlers = commandHandlers;
        this.alertScheduler = alertScheduler;
        this.alertScheduler.setAbsSender(this);

    }

    @Override
    public String getBotUsername() {
        return dotenv.get("TELEGRAM_BOT_USERNAME");

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