package com.oghenemalu.kryptobot.bot.handler;

import com.oghenemalu.kryptobot.bot.MenuBuilder;
import com.oghenemalu.kryptobot.price.CoinRegistry;
import com.oghenemalu.kryptobot.price.PriceService;
import com.oghenemalu.kryptobot.price.dto.PriceDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class PriceHandler implements CommandHandler{
    private final MenuBuilder menuBuilder;
    private final CoinRegistry coinRegistry;
    private final PriceService priceService;
    private static final Logger log = LoggerFactory.getLogger(PriceHandler.class);

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/quickprice")
                || command.startsWith("/price")
                || command.startsWith("/coins");
    }
    @Override
    public void handle(Update update, AbsSender absSender) {
        String command = update.getMessage().getText();
        if(command.startsWith("/quickprice")) {
            handleGetQuickPrice(update, absSender);
        } else if(command.startsWith("/coins")) {
            handleSupportedCoins(update, absSender);
        }else if(command.startsWith("/price")) {
            handleGetPrice(update, absSender);
        }
    }

    private void handleGetQuickPrice(Update update, AbsSender absSender) {
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

    private void handleGetPrice(Update update, AbsSender absSender) {
        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();
        StringBuilder response = new StringBuilder();
        String [] messageParts = message.split("\\s+");

        if (messageParts.length != 3) {
            response.append("""
                    Please enter price in the correct format\s
                    Correct format: /price <Coin Symbol> <Currency: USD, EUR, GBP supported>
                    Example: /price BTC USD""");
            tryExecute(absSender, menuBuilder.sendMessage(chatId, response.toString()));
            return;
        }
        String symbol = messageParts[1].toUpperCase();
        String currency = messageParts[2].toUpperCase();
        try {

            PriceDto priceDto =  priceService.getPrice(symbol, currency);
            if (priceDto.hasError()) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId,
                        "Error getting price for " + symbol + ": " + priceDto.getError()));
                return;
            }

            response.append(String.format(
                    "The price for %s is %s%s\n24h change: %s%%",
                    priceDto.getCoinSymbol(),
                    priceDto.getCurrencySymbol(),
                    formatPrice(priceDto.getPrice()),
                    priceDto.getChange24()));
            tryExecute(absSender, menuBuilder.sendMessage(chatId, response.toString()));
        } catch (Exception e) {
            response.append("There was an error trying to get the price for " + symbol + " " + currency +"\n" +
                    "Please try again later.");
            tryExecute(absSender, menuBuilder.sendMessage(chatId, response.toString()));
            log.info("Error: " + e.getMessage() + e);
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ONE) >= 0) {
            return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
        } else {
            return price.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
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
