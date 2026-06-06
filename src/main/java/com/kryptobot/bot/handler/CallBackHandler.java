package com.kryptobot.bot.handler;

import com.kryptobot.bot.MenuBuilder;
import com.kryptobot.price.PriceService;
import com.kryptobot.price.dto.PriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallBackHandler {
    private final MenuBuilder menuBuilder;
    private final Map<Long, String> userSelections = new ConcurrentHashMap<>();
    private final PriceService cryptoPriceService;


    public void handleCallback(CallbackQuery callbackQuery, AbsSender absSender) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        try {
            if (data.startsWith("currency:")) {
                userSelections.put(chatId, data.split(":")[1]);
                absSender.execute(menuBuilder.selectCoinMenu(chatId));

            } else if (data.startsWith("coin:")) {
                String currency = userSelections.get(chatId);
                String coin = data.split(":")[1];
                PriceDto priceDto = cryptoPriceService.getPrice(coin, currency);
                String message = String.format("The current price for %s is %s %s", coin, currency, priceDto.getPrice().toPlainString());
                absSender.execute(menuBuilder.sendMessage(chatId, message));

            }
        } catch (TelegramApiException e) {
            log.error("Failed to handle callback for chatId {}: {}", chatId, e.getMessage(), e);
        }
    }
}
