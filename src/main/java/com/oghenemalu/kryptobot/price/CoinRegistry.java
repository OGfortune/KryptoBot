package com.oghenemalu.kryptobot.price;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoinRegistry {

    @Getter
    private Map<String, String> coinMap = new HashMap<>();
    private final String apiKey;
    private final Logger log = LoggerFactory.getLogger(CoinRegistry.class.getName());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CoinRegistry(@Value("${coingecko.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @PostConstruct
    public void init() {
        loadCoinList();
    }

    private void loadCoinList() {
        try {
            String url = "https://api.coingecko.com/api/v3/coins/markets"
                    + "?vs_currency=usd&order=market_cap_desc&per_page=100&page=1";

            HttpResponse<String> response = Unirest.get(url)
                    .header("x-cg-demo-api-key", apiKey)
                    .asString();

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            Map<String, String> temp = new LinkedHashMap<>();
            jsonNode.forEach(node -> {
                String symbol = node.get("symbol").asText().toUpperCase();
                String id = node.get("id").asText();
                temp.putIfAbsent(symbol, id);
            });
            coinMap = temp;
            log.info("Loaded {} coins", coinMap.size());
        } catch (Exception e) {
            log.error("Failed to load coin list: {}", e.getMessage());
        }
    }

    public boolean isValidSymbol(String symbol) {
        return coinMap.containsKey(symbol.toUpperCase());
    }

    public Map<String, String> getAllCoins() {
        //returning a copy of the map to prevent concurrent modification exceptions
        return new LinkedHashMap<>(coinMap);
    }
    public String getCoinId(String symbol) {
        return coinMap.get(symbol.toUpperCase());
    }
}
