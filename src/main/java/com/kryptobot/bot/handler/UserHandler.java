package com.kryptobot.bot.handler;


import com.kryptobot.alert.AlertService;
import com.kryptobot.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserHandler implements CommandHandler {
    private final UserService userService;
    private final AlertService alertService;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/deleteuser");
    }

    @Override
    public void handle(Update update, AbsSender absSender) {
        String command = update.getMessage().getText();
        if (command.startsWith("/deleteuser")) {
            deleteUser(update, absSender);
        }
    }

    public void deleteUser(Update update, AbsSender absSender) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getFrom().getUserName();
        StringBuilder response = new StringBuilder();
        try {
            userService.deleteUser(userId);
            alertService.deleteAlert(chatId);

            response.append(String.format("""
                    Hi: %s
                    Your account and all your data (including alerts) has been deleted successfully.
                    Thank you for using KryptoBot.
                    """, userId));
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", userName, e.getMessage(), e);
        }

    }

    public void tryExecute(AbsSender absSender, SendMessage sendMessage) {
        try {
            absSender.execute(sendMessage);
        } catch (Exception e) {
            log.error("Failed to send message to chat {}: {}",
                    sendMessage.getChatId(), e.getMessage(), e);
        }
    }
}
