package com.oghenemalu.kryptobot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.core.HttpResponse;

import kong.unirest.core.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.Locale;


@Service
public class CryptoPriceService {


    private final String apiKey;

    public CryptoPriceService() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("COIN_GECKO_KEY");
    }

    public String getPrice(String coin, String currency) {

        String url = String.format("https://api.coingecko.com/api/v3/simple/price?vs_currencies=%s&ids=%s",
                currency, coin);

        HttpResponse<String> response = Unirest.get(url)
                .header("x-cg-demo-api-key", apiKey)
                .asString();

        String responseBody = response.getBody();
        System.out.println(responseBody);
        System.out.println(coin + " " + currency);

        try {
            JsonNode node = new ObjectMapper().readTree(response.getBody());

            double price = node.get(coin).get(currency.toLowerCase()).asDouble();
            System.out.println(price + " " + currency);

            String coinName = coin.substring(0, 1).toUpperCase() + coin.substring(1);
            String symbol = getCurrencySymbol(currency);

            System.out.println(coinName + " " + symbol + " " + price);

            return String.format("%s: %s%.2f", coinName, symbol, price);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Error fetching price for " + coin;
        }

    }

    private String getCurrencySymbol(String currency) {
        return switch (currency.toLowerCase()) {
            case "usd" -> "$";
            case "eur" -> "€";
            case "gbp" -> "£";
            default -> currency.toUpperCase() + " ";
        };

    }
}
