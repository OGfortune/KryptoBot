package com.oghenemalu.kryptobot.alerts;

import com.oghenemalu.kryptobot.enums.ConditionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AlertService {

    private AlertRepo alertRepo;
    public void createAlert(Long userId, String symbol, ConditionType conditionType, BigDecimal targetPrice) {
        Alerts alerts = new Alerts();
        alerts.setUserId(userId);
        alerts.setSymbol(symbol);
        alerts.setConditionType(conditionType);
        alerts.setTargetPrice(targetPrice);
        alerts.setActive(true);
        alertRepo.save(alerts);
    }

    public List<Alerts> getAlertsByUser(Long userId) {
        return alertRepo.getAlertsByUserId(userId);
    }

    public List<Alerts> getActiveAlertsByUser(Long userId) {
        return alertRepo.getActiveAlertsByUser(userId);
    }

    public List<Alerts> getAllActiveAlerts() {
        return alertRepo.getAllActiveAlerts();
    }

    public int getTotalAlerts(Long userId) {
        return alertRepo.countByUserId(userId);
    }

    public boolean deleteAlert(long Id) {
        alertRepo.deleteById(Id);
        return true;
    }
}
