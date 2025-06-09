package com.example.reminderbot.service;

import com.example.reminderbot.model.NotificationTask;
import com.example.reminderbot.repository.NotificationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component

public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    @Value("${bot.name}")
    private String botName;

    @Autowired
    private NotificationTaskRepository taskRepository;

    // Паттерн для разбора команды /remind
    private static final Pattern REMIND_PATTERN = Pattern.compile("(/remind)\\s+(.+?)\\s+через\\s+(\\d+)\\s+(minute|hour|day)s?");
    private static final Pattern REMIND_PATTERN_DATE = Pattern.compile("(/remind)\\s+((?:\\d{2}\\.\\d{2}(?:\\.\\d{4})?)\\s+\\d{2}:\\d{2})\\s+(.+)");

    public TelegramBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equals("/start")) {
                sendStartMessage(chatId);
            } else if (messageText.startsWith("/remind")) {
                handleRemindCommand(chatId, messageText);
            } else if (messageText.equals("/list")) {
                handleListCommand(chatId);
            } else {
                sendMessage(chatId, "Неизвестная команда. Введите /start, чтобы узнать, как пользоваться ботом.");
            }
        }
    }


    private void sendStartMessage(long chatId) {
        String startText = "Привет! Я бот для создания напоминаний.\n\n" +
                "Вот как мной пользоваться:\n\n" +
                "*/remind* - поставить напоминание.\n" +
                "*/list* - показать список всех активных напоминаний.\n\n" +
                "Например:\n" +
                "`/remind Позвонить маме через 2 hours`\n" +
                "или\n" +
                "`/remind 15.12.2025 10:00 Записаться к врачу`";

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(startText);
        // Включаем поддержку Markdown, чтобы *, _ и ` работали для форматирования
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки стартового сообщения: {}", e.getMessage());
        }
    }

    private void handleListCommand(long chatId) {
        List<NotificationTask> tasks = taskRepository.findByChatIdAndNotificationDateTimeAfter(chatId, LocalDateTime.now());
        if (tasks.isEmpty()) {
            sendMessage(chatId, "У вас нет активных напоминаний.");
            return;
        }

        StringBuilder sb = new StringBuilder("Ваши активные напоминания:\n\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        for (NotificationTask task : tasks) {
            sb.append("- ")
                    .append(task.getMessage())
                    .append(" (")
                    .append(task.getNotificationDateTime().format(formatter))
                    .append(")\n");
        }
        sendMessage(chatId, sb.toString());
    }

    private void handleRemindCommand(long chatId, String text) {
        // Попытка разобрать формат "in X minutes/hours/days"
        Matcher matcher = REMIND_PATTERN.matcher(text);
        if (matcher.matches()) {
            String message = matcher.group(2);
            int amount = Integer.parseInt(matcher.group(3));
            String unit = matcher.group(4);

            LocalDateTime dateTime = LocalDateTime.now();
            switch (unit) {
                case "minute": dateTime = dateTime.plusMinutes(amount); break;
                case "hour": dateTime = dateTime.plusHours(amount); break;
                case "day": dateTime = dateTime.plusDays(amount); break;
            }
            saveTask(chatId, message, dateTime);
            return;
        }

        // Попытка разобрать формат "dd.MM HH:mm message"
        Matcher dateMatcher = REMIND_PATTERN_DATE.matcher(text);
        if (dateMatcher.matches()) {
            try {
                String dateTimeStr = dateMatcher.group(2).trim();
                String message = dateMatcher.group(3).trim();
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                // Добавляем год, если он не указан
                if (dateTime.getYear() == 0) {
                    dateTime = dateTime.withYear(LocalDateTime.now().getYear());
                }
                if (dateTime.isBefore(LocalDateTime.now())) {
                    dateTime = dateTime.plusYears(1);
                }

                saveTask(chatId, message, dateTime);
            } catch (DateTimeParseException e) {
                sendUsage(chatId);
            }
            return;
        }

        sendUsage(chatId);
    }

    private void saveTask(long chatId, String message, LocalDateTime dateTime) {
        NotificationTask task = new NotificationTask();
        task.setChatId(chatId);
        task.setMessage(message);
        task.setNotificationDateTime(dateTime);
        taskRepository.save(task);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        sendMessage(chatId, "Хорошо, я напомню вам: \"" + message + "\" в " + dateTime.format(formatter));
        log.info("Создана задача: {}", task);
    }

    private void sendUsage(long chatId) {
        sendMessage(chatId, "Неверный формат. Попробуйте так:\n" +
                "`/remind Позвонить маме через 2 hours`\n" +
                "или\n" +
                "`/remind 15.12.2025 10:00 Записаться к врачу`");
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения: {}", e.getMessage());
        }
    }
}