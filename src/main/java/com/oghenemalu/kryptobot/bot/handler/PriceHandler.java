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
public class PriceHandler implements CommandHandler{
    private final MenuBuilder menuBuilder;
    private final CoinRegistry coinRegistry;
    private static final Logger log = LoggerFactory.getLogger(PriceHandler.class);

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/price")
                || command.startsWith("/coins");
    }
    @Override
    public void handle(Update update, AbsSender absSender) {
        String command = update.getMessage().getText();
        if(command.startsWith("/price")) {
            handleGetPrice(update, absSender);
        } else if(command.startsWith("/coins")) {
            handleSupportedCoins(update, absSender);
        }
    }

    private void handleGetPrice(Update update, AbsSender absSender) {
        Long chatId = update.getMessage().getChatId();
        tryExecute(absSender, menuBuilder.selectCurrency(chatId));
    }

    private void handleSupportedCoins(Update update, AbsSender absSender) {
        try {
            Long chatId = update.getMessage().getChatId();
            StringBuilder response = new StringBuilder();
            response.append("Here are the supported coins 🪙✅: \n" + "Number: Symbol - Name \n \n");
            int counter = 1;
            for (String coin : coinRegistry.getAllCoins().keySet()) {
                response.append(String.format("%d - %s - %s\n", counter, coin, coinRegistry.getCoinMap().get(coin)));
                counter++;
            }
            tryExecute(absSender, menuBuilder.sendMessage(chatId, response.toString()));
        } catch (Exception e) {
            log.error("Error handling supported coins command for user {}", e.getMessage(), e);
        }

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
