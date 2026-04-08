package com.oghenemalu.kryptobot.alerts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlertRepo extends JpaRepository<Alerts, Integer> {
    int countByUserId(Long userId);

    List<Alerts> getAlertsByUserId(Long userId);

    @Query(value = """
            SELECT * FROM alerts AS a
                        WHERE a.user_id = :userId
                        AND a.is_active = true
                        
            """, nativeQuery = true)
    List<Alerts> getActiveAlertsByUser(Long userId);

    List<Alerts> getAllActiveAlerts();

    List<Alerts> deleteById(Long Id);
}
