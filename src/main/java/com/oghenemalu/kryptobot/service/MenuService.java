package com.oghenemalu.kryptobot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


@Service
public class MenuService {
    public SendMessage selectCoinMenu(long chatId) {
        InlineKeyboardMarkup coinMarkUp = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("Bitcoin").callbackData("coin:bitcoin").build(),
                        InlineKeyboardButton.builder().text("Ethereum").callbackData("coin:ethereum").build(),
                        InlineKeyboardButton.builder().text("Solana").callbackData("coin:solana").build()
                ))
                .build();

        return SendMessage.builder()
                .chatId(chatId)
                .text("Select Coin")
                .replyMarkup(coinMarkUp)
                .build();
    }

    public SendMessage selectCurrency(long chatId) {
        InlineKeyboardMarkup currencyMarkUp = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(
                        InlineKeyboardButton.builder().text("EUR").callbackData("currency:EUR").build(),
                        InlineKeyboardButton.builder().text("USD").callbackData("currency:USD").build(),
                        InlineKeyboardButton.builder().text("GBP").callbackData("currency:GBP").build()
                ))
                .build();

        return SendMessage.builder()
                .chatId(chatId)
                .text("Select Currency")
                .replyMarkup(currencyMarkUp)
                .build();

    }


    public SendMessage sendMessage(long chatId, String message) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();

    }


}
