package com.oghenemalu.kryptobot.bot.handler;

import com.oghenemalu.kryptobot.bot.MenuService;
import com.oghenemalu.kryptobot.price.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CallBackHandler {
    private final MenuService menuService;
    private final Map<Long, String> userSelections = new ConcurrentHashMap<>();
    private final PriceService cryptoPriceService;

    public void handleCallback(CallbackQuery callbackQuery, AbsSender absSender) {
        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        try {
            if (data.startsWith("currency:")) {
                userSelections.put(chatId, data.split(":")[1]);
                absSender.execute(menuService.selectCoinMenu(chatId));

            } else if (data.startsWith("coin:")) {
                String currency = userSelections.get(chatId);
                String coin = data.split(":")[1];
                String price = cryptoPriceService.getPrice(coin, currency);
                absSender.execute(menuService.sendMessage(chatId, price));

            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
