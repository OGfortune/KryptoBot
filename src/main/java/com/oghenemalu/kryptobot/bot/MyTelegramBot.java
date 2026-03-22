package com.oghenemalu.kryptobot.bot;

import com.oghenemalu.kryptobot.service.CallBackService;
import com.oghenemalu.kryptobot.service.CryptoPriceService;
import com.oghenemalu.kryptobot.service.MenuService;
import com.oghenemalu.kryptobot.user.UserEntity;
import com.oghenemalu.kryptobot.user.UserService;
import io.github.cdimascio.dotenv.Dotenv;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;



@Component

public class MyTelegramBot extends TelegramLongPollingBot {

    private final Dotenv dotenv = Dotenv.load();

    private final MenuService menuService;
    private final CallBackService callBackService;

    private final UserService userService;

    public MyTelegramBot(MenuService menuService, CallBackService callBackService, UserService userService) {
        super(Dotenv.load().get("TELEGRAM_TOKEN"));

        String token = dotenv.get("TELEGRAM_TOKEN");
        String username = dotenv.get("TELEGRAM_BOT_USERNAME");

        System.out.println("=".repeat(50));
        System.out.println("🤖 BOT INITIALIZATION");
        System.out.println("=".repeat(50));
        System.out.println("Token loaded: " + ( token != null ? "✅ YES (length: " + token.length() + ")" : "❌ NO"));
        System.out.println("Username: " + (username != null ? username : "❌ NOT SET"));
        System.out.println("=".repeat(50));

        this.menuService = menuService;
        this.callBackService = callBackService;
        this.userService = userService;
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

            System.out.println(userId + " " + chatId + " " + message);
            if(message.startsWith("/start")) {
                tryExecute(menuService.sendMessage(chatId, "Welcome to Kryptobot!"));
            } else if (message.startsWith("/price")) {
                tryExecute(menuService.selectCurrency(chatId));
            } else if (message.startsWith("/createAlert")) {
                SendMessage sendMessage = new SendMessage();
                userService.getOrCreateUser(userId, chatId, userName);
                tryExecute(menuService.sendMessage(chatId, "Alert created!"));

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