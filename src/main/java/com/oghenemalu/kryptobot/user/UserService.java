package com.oghenemalu.kryptobot.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    public UserEntity getOrCreateUser(Long userId, Long chatId, String userName) {
        Optional<UserEntity> userEntity = userRepo.findById(userId);
        return userEntity.orElseGet(() -> userRepo.save(new UserEntity(userId, chatId, userName)));
    }
}
