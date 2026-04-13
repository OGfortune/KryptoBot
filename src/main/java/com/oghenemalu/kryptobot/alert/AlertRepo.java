package com.oghenemalu.kryptobot.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepo extends JpaRepository<Alert, Long> {
    int countByUserId(Long userId);
    List<Alert> findByUserId(Long userId);
    List<Alert> findByUserIdAndActiveTrue(Long userId);
    List<Alert> findByActiveTrue();
}
