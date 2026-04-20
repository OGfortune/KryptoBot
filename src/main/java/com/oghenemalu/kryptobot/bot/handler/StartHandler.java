package com.oghenemalu.kryptobot.bot.handler;

import com.oghenemalu.kryptobot.bot.MenuBuilder;
import com.oghenemalu.kryptobot.price.CoinRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
@RequiredArgsConstructor
public class StartHandler implements CommandHandler {
    private final MenuBuilder menuBuilder;
    private static final Logger log = LoggerFactory.getLogger(StartHandler.class.getName());


    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/start")
                || command.startsWith("/help");
    }

    @Override
    public void handle(Update update, AbsSender absSender) {
        String command = update.getMessage().getText();
        if(command.startsWith("/start")) {
            handleStart(update, absSender);
        }  else if (command.startsWith("/help")) {
            handleHelp(update, absSender);
        }
    }

    private void handleStart(Update update, AbsSender absSender) {
        Long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getUserName();

        String response;

        response = String.format("""
                Hello, %s!\s
                Welcome, My name is KryptoBot, and I am here to help you with some of your \
                cryptocurrency services and management \
                This includes getting current price 📈📉\s
                🔔 setting alerts and getting notified""", userName);

        try {
            tryExecute(absSender, menuBuilder.sendMessage(chatId, response));
        } catch (Exception e) {
            response = String.format("""
                    Hello, %s!\s
                    We are currently experiencing an issue with starting the bot, please try again later.
                    """, userName);
            tryExecute(absSender, menuBuilder.sendMessage(chatId, response));
            log.error("Error handling command for user {}: {}", userName, e.getMessage(), e);
        }
    }



    private void handleHelp(Update update, AbsSender absSender) {
        Long chatId = update.getMessage().getChatId();
        String helpMessage = """
                Here are the available commands:\s
                /start - Start the bot\s
                /coins - List supported coins\s
                /help - Show this help message\s
                /price - Get price of a coin\s
                /setalert - Allows user to create alert format:/setalert <coin_symbol> <condition> <alert_price>\s
                /getalerts - Returns all the users active alerts\s
                /deletealert - Allows user to delete an alert format:/deletealert <alert_id>""";

        tryExecute(absSender, menuBuilder.sendMessage(chatId, helpMessage));
    }

    private void tryExecute(AbsSender absSender, SendMessage sendMessage) {
        try {
            absSender.execute(sendMessage);
        } catch (Exception e) {
            log.error("Failed to send message to chat {}: {}",
                    sendMessage.getChatId(), e.getMessage(), e);
        }
    }
}
