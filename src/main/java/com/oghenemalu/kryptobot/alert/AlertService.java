package com.oghenemalu.kryptobot.alert;

import com.oghenemalu.kryptobot.enums.ConditionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepo alertRepo;
    public void createAlert(Long userId, Long chatId, String symbol, ConditionType conditionType, BigDecimal targetPrice) {
        Alert alert = new Alert();
        alert.setUserId(userId);
        alert.setChatId(chatId);
        alert.setSymbol(symbol);
        alert.setConditionType(conditionType);
        alert.setTargetPrice(targetPrice);
        alert.setActive(true);
        alertRepo.save(alert);
        System.out.println("Alert created: " + alert);
    }

    public List<Alert> getAlertsByUser(Long userId) {
        return alertRepo.findByUserId(userId);
    }

    public List<Alert> getActiveAlertsByUser(Long userId) {
        return alertRepo.findByUserIdAndActiveTrue(userId);
    }

    public List<Alert> getAllActiveAlerts() {
        return alertRepo.findByActiveTrue();
    }

    public int getTotalAlerts(Long userId) {
        return alertRepo.countByUserId(userId);
    }

    public boolean deleteAlert(long Id) {
        alertRepo.deleteById(Id);
        return true;
    }
}
