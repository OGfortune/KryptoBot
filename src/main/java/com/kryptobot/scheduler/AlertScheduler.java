package com.kryptobot.scheduler;

import com.kryptobot.alert.Alert;
import com.kryptobot.alert.AlertService;
import com.kryptobot.bot.MenuBuilder;
import com.kryptobot.price.CoinRegistry;
import com.kryptobot.price.PriceService;
import com.kryptobot.price.dto.PriceDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {
    private final PriceService priceService;
    private final AlertService alertService;
    private final MenuBuilder menuBuilder;
    private final CoinRegistry coinRegistry;



    @Setter
    private AbsSender absSender;




    @Scheduled(fixedRate = 60000)
    public void checkAlerts() {
        if (absSender == null) {
            return;
        }
        log.info("Checking alerts...");

        //create a list of active alerts
        List<Alert> activeAlerts = alertService.getAllActiveAlerts();
        if (activeAlerts.isEmpty()) {
            return;
        }

        //group active alerts by symbol
        Map<String, List<Alert>> alertsBySymbol = activeAlerts.stream()
                .collect(Collectors.groupingBy(Alert::getSymbol));

        //check each alert
        alertsBySymbol.forEach((symbol, alerts) -> {
            PriceDto priceDto = priceService.getPrice(coinRegistry.getCoinId(symbol), "USD");
            if (priceDto.hasError()) {
                return;
            }

            //check if alert should fire
            for (Alert alert : alerts) {
                try {
                    //send alert if it should fire
                    if (alert.shouldFire(priceDto.getPrice())) {
                        String message = String.format("Alert has been triggered for %s. The current price of $%s is currently %s " +
                                "or EQUALS your  target price of $%s, and the alert is now inactive. \n"
                                + "use /getalerts to view all your active alerts",
                                alert.getSymbol(), priceDto.getPrice(), alert.getConditionType(), alert.getTargetPrice());
                        absSender.execute(menuBuilder.sendMessage(alert.getChatId(), message));
                        alert.setActive(false);
                        alertService.updateAlert(alert);
                    }
                } catch (Exception e) {
                    log.error("Error checking alert: {}", e.getMessage(), e);
                }
            }
        });

    }
}
