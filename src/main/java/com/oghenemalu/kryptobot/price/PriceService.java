package com.oghenemalu.kryptobot.price;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oghenemalu.kryptobot.price.dto.PriceDto;
import io.github.cdimascio.dotenv.Dotenv;
import kong.unirest.core.HttpResponse;

import kong.unirest.core.Unirest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Service
public class PriceService {


    private final String apiKey;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CoinRegistry coinRegistry;

    public PriceService(CoinRegistry coinRegistry) {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("COIN_GECKO_KEY");
        this.coinRegistry = coinRegistry;
    }

    @Cacheable("price")
    public PriceDto getPrice(String coin, String currency) {
        return getCryptoPrice(coin, currency);
    }

    private PriceDto getCryptoPrice(String coin, String currency) {
        try {
            String url = String.format("https://api.coingecko.com/api/v3/simple/price?vs_currencies=%s&ids=%s&include_24hr_change=true",
                    currency, coin);

            HttpResponse<String> response = Unirest.get(url)
                    .header("x-cg-demo-api-key", apiKey)
                    .asString();

            //check if response is successful
            if (!response.isSuccess()) {
                return PriceDto.error(coin, currency, "❌Error fetching price: "
                        + response.getStatusText() + " for " + coin);
            }

            System.out.println(response.getBody());

            //parse response
            JsonNode node = objectMapper.readTree(response.getBody());

            //check if coin is in response
            JsonNode coinNode = node.get(coin.toLowerCase());
            if (coinNode == null) {
                return PriceDto.error(coin, currency, "❌Error fetching price for: " + coin);
            }

            //check if currency is in response
            JsonNode currencyNode = coinNode.get(currency.toLowerCase());
            if (currencyNode == null) {
                return PriceDto.error(coin, currency, "❌Error fetching price for: " + currency);
            }

            //get price and change
            BigDecimal price = currencyNode.decimalValue();
            String changeKey = currency.toLowerCase() + "_24h_change";
            BigDecimal change24 = coinNode.has(changeKey) ?
                    coinNode.get(changeKey).decimalValue() : BigDecimal.ZERO;

            //get currency symbol
            String currencySymbol = getCurrencySymbol(currency);

            //return price
            return PriceDto.ok(coin, currencySymbol, price, change24);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //return error
            return PriceDto.error(coin, currency, "❌Error fetching price: " + e.getMessage() + " for " + coin);
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
