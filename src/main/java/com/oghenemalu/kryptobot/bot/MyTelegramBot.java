package com.oghenemalu.kryptobot.bot;

import com.oghenemalu.kryptobot.bot.handler.CallBackHandler;
import com.oghenemalu.kryptobot.bot.handler.CommandHandler;
import com.oghenemalu.kryptobot.user.UserService;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final Dotenv dotenv = Dotenv.load();

    private final MenuBuilder menuBuilder;
    private final CallBackHandler callBackService;
    private final List<CommandHandler> commandHandlers;

    private final UserService userService;

    public MyTelegramBot(MenuBuilder menuBuilder,
                         CallBackHandler callBackService,
                         UserService userService,
                         List<CommandHandler> commandHandlers) {
        super(Dotenv.load().get("TELEGRAM_TOKEN"));

        String token = dotenv.get("TELEGRAM_TOKEN");
        String username = dotenv.get("TELEGRAM_BOT_USERNAME");

        System.out.println("=".repeat(50));
        System.out.println("🤖 BOT INITIALIZATION");
        System.out.println("=".repeat(50));
        System.out.println("Token loaded: " + ( token != null ? "✅ YES (length: " + token.length() + ")" : "❌ NO"));
        System.out.println("Username: " + (username != null ? username : "❌ NOT SET"));
        System.out.println("=".repeat(50));

        this.menuBuilder = menuBuilder;
        this.callBackService = callBackService;
        this.userService = userService;
        this.commandHandlers = commandHandlers;
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
            String userName = update.getMessage().getFrom().getUserName();

            commandHandlers.forEach(handler -> {
                if(handler.canHandle(message)) {
                    handler.handle(update, this);
                }
            });

            System.out.println(userId + " " + chatId + " " + message);
            if(message.startsWith("/start")) {
                tryExecute(menuBuilder.sendMessage(chatId, "Welcome to Kryptobot!"));
            } else if (message.startsWith("/price")) {
                tryExecute(menuBuilder.selectCurrency(chatId));
            } else if (message.startsWith("/createAlert")) {
                userService.getOrCreateUser(userId, chatId, userName);
                tryExecute(menuBuilder.sendMessage(chatId, "Alert created!"));
            }
        } else if (update.hasCallbackQuery()) {
            callBackService.handleCallback(update.getCallbackQuery(), this);
        }
    }

    public void tryExecute(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}