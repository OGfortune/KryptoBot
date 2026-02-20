package com.oghenemalu.kryptobot.service;

import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CryptoPriceService {

    public String getPrice(String coin, String currency) {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("PRICE");
        String url = String.format("https://api.coingecko.com/api/v3/simple/price?vs_currencies=%s&ids=%s",
                currency, coin);

        HttpResponse<String> response = Unirest.get(url)
                .header("x-cg-demo-api-key", apiKey)
                .asString();

        return response.getBody();
    }
}
