package com.oghenemalu.kryptobot.price.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceDto {
    private String currencySymbol;
    private String coinSymbol;
    private BigDecimal price;
    private BigDecimal change24;
    private String error;

    public static PriceDto ok(String currencySymbol, String coinSymbol, BigDecimal price, BigDecimal change24) {
        PriceDto dto = new PriceDto();
        dto.coinSymbol = coinSymbol;
        dto.currencySymbol = currencySymbol;
        dto.price = price;
        dto.change24 = change24;
        return dto;
    }


    public static PriceDto error(String coinSymbol, String currencySymbol, String error) {
        PriceDto dto = new PriceDto();
        dto.coinSymbol = coinSymbol;
        dto.currencySymbol = currencySymbol;
        dto.error = error;
        return dto;
    }

    public boolean error() {
        return error != null;
    }
}
