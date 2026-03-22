package com.oghenemalu.kryptobot.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")  // Map to "users" table
@Data                   // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor      // No-args constructor (required by JPA)
@AllArgsConstructor     // All-args constructor
public class UserEntity {

    @Id
    @Column(name = "user_id")  // Maps to user_id in database
    private Long userId;        // This is the Telegram user ID (PRIMARY KEY)

    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;        // Telegram chat ID

    @Column(name = "username")
    private String username;    // Telegram username

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist  // Automatically set createdAt before saving
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Constructor matching what UserService calls
    public UserEntity(Long userId, Long chatId, String username) {
        this.userId = userId;
        this.chatId = chatId;
        this.username = username;
    }
}