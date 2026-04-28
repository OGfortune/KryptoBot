package com.oghenemalu.kryptobot.bot.handler;

import com.oghenemalu.kryptobot.alert.Alert;
import com.oghenemalu.kryptobot.alert.AlertRepo;
import com.oghenemalu.kryptobot.alert.AlertService;
import com.oghenemalu.kryptobot.bot.MenuBuilder;
import com.oghenemalu.kryptobot.enums.ConditionType;
import com.oghenemalu.kryptobot.price.CoinRegistry;
import com.oghenemalu.kryptobot.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AlertsHandler implements CommandHandler {
    private final AlertService alertService;
    private final MenuBuilder menuBuilder;
    private final CoinRegistry coinRegistry;


    private static final Logger log = LoggerFactory.getLogger(AlertsHandler.class);
    private final UserService userService;


    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/setalert") ||
                command.startsWith("/getalerts") ||
                command.startsWith("/deletealert");
    }

    @Override
    public void handle(Update update, AbsSender absSender) {
        String command = update.getMessage().getText();
        if (command.startsWith("/setalert")) {
            handleSetAlert(update, absSender);
        } else if (command.startsWith("/getalerts")) {
            handleGetAlert(update, absSender);
        } else if (command.startsWith("/deletealert")) {
            handleDeleteAlert(update, absSender);
        }
    }

    private void handleSetAlert(Update update, AbsSender absSender) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String message = update.getMessage().getText();
        String[] messageParts = message.trim().split("\\s+");

        if (messageParts.length != 4) {
            tryExecute(absSender, menuBuilder.sendMessage(chatId,
                    "❌ Invalid format.\n\n" +
                            "*Usage:*\n" +
                            "`/setalert <symbol> <above/below> <price>`\n\n" +
                            "*Examples:*\n" +
                            "`/setalert btc above 50000`\n" +
                            "`/setalert eth below 2000`\n"));
            return;
        }


        try {
            String symbol = messageParts[1].toUpperCase();
            String condition = messageParts[2].toUpperCase();
            String price = messageParts[3];

            if (!coinRegistry.isValidSymbol(symbol)) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId, "❌ " + symbol + " is not a valid symbol. or is not supported yet." +
                        "We support the top 100 coins by market cap.\n" +
                        "Common examples: BTC, ETH, SOL, BNB, XRP, ADA, DOGE\n\n" +
                        "Use /coins to see the full list."));
                return;
            }

            BigDecimal targetPrice;
            try {
                targetPrice = new BigDecimal(price);
                if (targetPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    tryExecute(absSender, menuBuilder.sendMessage(chatId, "❌ Price must be greater than 0"));
                    return;
                }
            } catch (NumberFormatException e) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId,
                        "❌ Invalid price format. Use numbers only.\n\n" +
                                "Example: /setalert BTC above 50000"));
                return;
            }

            ConditionType conditionType;
            try {
                conditionType = ConditionType.valueOf(condition.toUpperCase());
            } catch (IllegalArgumentException e) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId, "❌ Invalid condition type. Use 'above' or 'below'."));
                return;
            }
            alertService.createAlert(userId, chatId, symbol, conditionType, targetPrice);

            String successMessage = String.format("""
                            ✅ Alerts has been created successfully!\s
                            
                            Your new alert is for: %s %s %s\
                            You'll be notified when the price is triggered.
                            Use /alert to see all your alert.""",
                    symbol, conditionType, targetPrice);
            tryExecute(absSender, menuBuilder.sendMessage(chatId, successMessage));
        } catch (Exception e) {
            tryExecute(absSender, menuBuilder.sendMessage(chatId, "❌ Error creating alert for: " + e.getMessage()));
            log.error("Error creating alert for user {}: {}", userName, e.getMessage(), e);
        }
    }

    private void handleGetAlert(Update update, AbsSender absSender) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();

        try {
            List<Alert> alert = alertService.getAlertsByUser(userId);

            if (alert.isEmpty()) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId,
                        "You have no active alert at the moment for: " + userName +
                                "\nCreate one with:\n" +
                                "/setalert <symbol> <above/below> <price>\n\n" +
                                "Example: /setalert BTC above 50000"));
                return;
            }

            StringBuilder response = new StringBuilder("🔔 *Your Active Alerts: \n");
            for (int i = 0; i < alert.size(); i++) {
                Alert alerts = alert.get(i);
                response.append(String.format(
                        """
                                %d* %s %s $%s\s
                                   Created: %s
                                
                                """,
                        i + 1,
                        alerts.getSymbol(),
                        alerts.getConditionType().name().toLowerCase(),
                        alerts.getTargetPrice(),
                        alerts.getCreatedAt().toLocalDate()
                ));
            }
            response.append("To delete: `/deletealert <number>`\n");
            response.append("Example: `/deletealert 1`");

            tryExecute(absSender, menuBuilder.sendMessage(chatId, response.toString()));
        } catch (Exception e) {
            tryExecute(absSender, menuBuilder.sendMessage(chatId, "❌ Error getting alert for: "
                    + userName
                    + " Please Try again later. " + e.getMessage()));
        }
    }

    private void handleDeleteAlert(Update update, AbsSender absSender) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String message = update.getMessage().getText();

        String[] parts = message.trim().split("\\s+");
        if (parts.length != 2) {
            tryExecute(absSender, menuBuilder.sendMessage(chatId,
                    "Usage: /deletealert <number>\n" +
                            "Use /alert to see your alert numbers"));
            return;
        }
        try {
            int alertId = Integer.parseInt(parts[1]);
            if (alertId < 1) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId,
                        "❌Please enter a valid alertToDelete number: " +
                                "Alert number must be greater than 0."));
                return;
            }

            List<Alert> alert = alertService.getAlertsByUser(userId);
            if (alert.isEmpty()) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId, "You have no active alert at the moment for: " + userName));
                return;
            }

            if (alertId > alert.size()) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId, "❌ Invalid alertToDelete number" +
                        "you have: " + alert.size() + " alertToDelete numbers. use /alert to see your alertToDelete numbers"));
            }

            Alert alertToDelete = alert.get(alertId - 1);

            boolean alertDeleted = alertService.deleteAlert(alertToDelete.getId());
            if (alertDeleted) {
                tryExecute(absSender, menuBuilder.sendMessage(chatId,
                        String.format("""
                                        Hi: %s\s
                                        ✅ Deleted alert:
                                        %s %s $%s""",
                                userName,
                                alertToDelete.getSymbol(),
                                alertToDelete.getConditionType().name().toLowerCase(),
                                alertToDelete.getTargetPrice())));
            } else {
                tryExecute(absSender, menuBuilder.sendMessage(chatId, "❌ Failed to delete alert."));
            }

        } catch (NumberFormatException e) {
            tryExecute(absSender, menuBuilder.sendMessage(chatId,
                    "❌ Invalid number format.\n" +
                            "Usage: /deletealert 1"));
        } catch (Exception e) {
            tryExecute(absSender, menuBuilder.sendMessage(chatId, "❌ Error deleting alert. Please try again."));
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
