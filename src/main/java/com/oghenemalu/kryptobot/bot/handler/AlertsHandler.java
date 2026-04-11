package com.oghenemalu.kryptobot.bot.handler;

import com.oghenemalu.kryptobot.alerts.AlertRepo;
import com.oghenemalu.kryptobot.alerts.AlertService;
import com.oghenemalu.kryptobot.alerts.Alerts;
import com.oghenemalu.kryptobot.bot.MenuBuilder;
import com.oghenemalu.kryptobot.enums.ConditionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AlertsHandler implements CommandHandler {
    private final AlertService alertService;
    private final MenuBuilder menuBuilder;

    private static final Set<String> CRYPTO_SYMBOLS = Set.of(
            "BTC", "ETH", "SOL", "BNB", "XRP", "ADA", "DOGE", "DOT", "MATIC", "AVAX"
    );
    private final AlertRepo alertRepo;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/setalerts") ||
                command.startsWith("/getalerts") ||
                command.startsWith("/deletealerts");
    }

    @Override
    public void handle(Update update) {
        String command = update.getMessage().getText();
        if (command.startsWith("/setalerts")) {
            handleSetAlert(update);
        } else if (command.startsWith("/getalerts")) {
            handleGetAlert(update);
        } else if (command.startsWith("/deletealerts")) {
            handleDeleteAlert(update);
        }
    }

    private void handleSetAlert(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String message = update.getMessage().getText();
        String [] messageParts = message.trim().split("\\s+");

        if (messageParts.length != 4) {
            menuBuilder.sendMessage(chatId,
                    "❌ Invalid format.\n\n" +
                            "*Usage:*\n" +
                            "`/setalert <symbol> <above/below> <price>`\n\n" +
                            "*Examples:*\n" +
                            "`/setalert BTC above 50000`\n" +
                            "`/setalert ETH below 2000`\n");
        }


        try {
            String symbol = messageParts[0].toUpperCase();
            String condition = messageParts[1].toUpperCase();
            String price = messageParts[2];

            if (!CRYPTO_SYMBOLS.contains(symbol)) {
                menuBuilder.sendMessage(chatId, symbol + " is not a valid symbol." +
                        "Please use one of the following: " + CRYPTO_SYMBOLS);
            }

            BigDecimal targetPrice;
            try {
                targetPrice = new BigDecimal(price);
                if (targetPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    menuBuilder.sendMessage(chatId, "❌ Price must be greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                menuBuilder.sendMessage(chatId,
                        "❌ Invalid price format. Use numbers only.\n\n" +
                                "Example: /setalert BTC above 50000");
                return;
            }

            ConditionType conditionType;
            try {
                conditionType = ConditionType.valueOf(condition.toUpperCase());
            } catch (IllegalArgumentException e) {
                menuBuilder.sendMessage(chatId, "❌ Invalid condition type. Use 'above' or 'below'.");
                return;
            }
            alertService.createAlert(userId, symbol, conditionType, targetPrice);

            String successMessage = String.format("""
                            ✅ Alerts has been created successfully!\s
                            
                            Your new alert is for: %s %s %s\
                            You'll be notified when the price is triggered.
                            Use /alerts to see all your alerts.""",
                    symbol, conditionType, targetPrice);
            menuBuilder.sendMessage(chatId, successMessage);
        } catch (Exception e) {
            menuBuilder.sendMessage(chatId, "❌ Error creating alert for: " + e.getMessage());
        }
    }
    private void handleGetAlert(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();

        try {
            List<Alerts> alerts = alertService.getAlertsByUser(userId);

            if(alerts.isEmpty()) {
                menuBuilder.sendMessage(chatId,
                        "You have no active alerts at the moment for: " + userName +
                                "\nCreate one with:\n" +
                                "/setalert <symbol> <above/below> <price>\n\n" +
                                "Example: /setalert BTC above 50000");
                return;
            }

            StringBuilder response = new StringBuilder("🔔 *Your Active Alerts:*\\n\\n");
            for (int i=0; i<alerts.size(); i++) {
                Alerts alert = alerts.get(i);
                response.append(String.format(
                        "*%d.* %s %s %s $%s\n" +
                                "   Created: %s\n\n",
                        i + 1,
                        alert.getSymbol(),
                        alert.getConditionType().name().toLowerCase(),
                        alert.getTargetPrice(),
                        alert.getCreatedAt().toLocalDate()
                ));
            }
            response.append("To delete: `/deletealert <number>`\n");
            response.append("Example: `/deletealert 1`");

            menuBuilder.sendMessage(chatId, response.toString());
        }  catch (Exception e) {
            menuBuilder.sendMessage(chatId, "❌ Error getting alerts for: "
                    + userName
                    + "Please Try again later.");
        }
    }
    private void handleDeleteAlert(Update update) {
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String message = update.getMessage().getText();

        String [] parts = message.trim().split("\\s+");
        if (parts.length != 2) {
            menuBuilder.sendMessage(chatId,
                    "Usage: /deletealert <number>\n\n" +
                            "Use /alerts to see your alert numbers");
            return;
        }
        try {
            int alertId = Integer.parseInt(parts[1]);
            if (alertId <= 1) {
                menuBuilder.sendMessage(chatId,
                        "❌Please enter a valid alertToDelete number: " +
                                "Alert number must be greater than 0.");
                return;
            }

            List<Alerts> alerts = alertService.getAlertsByUser(userId);
            if(alerts.isEmpty()) {
                menuBuilder.sendMessage(chatId, "You have no active alerts at the moment for: " + userName);
                return;
            }

            if (alertId > alerts.size()) {
                menuBuilder.sendMessage(chatId, "❌ Invalid alertToDelete number" +
                        "you have: " + alerts.size() + " alertToDelete numbers. use /alerts to see your alertToDelete numbers");
            }

            Alerts alertToDelete = alerts.get(alertId-1);

            boolean alertDeleted = alertService.deleteAlert(alertToDelete.getId());
            if(alertDeleted) {
                menuBuilder.sendMessage(chatId,
                        String.format("""
                                        Hi: %s\s
                                        ✅ Deleted alert:
                                        %s %s $%s""",
                                userName,
                                alertToDelete.getSymbol(),
                                alertToDelete.getConditionType().name().toLowerCase(),
                                alertToDelete.getTargetPrice()));
            } else {
                menuBuilder.sendMessage(chatId, "❌ Failed to delete alert.");
            }

        } catch (NumberFormatException e) {
            menuBuilder.sendMessage(chatId,
                    "❌ Invalid number format.\n\n" +
                            "Usage: /deletealert 1");
        } catch (Exception e) {
            menuBuilder.sendMessage(chatId, "❌ Error deleting alert. Please try again.");
        }
    }
}
