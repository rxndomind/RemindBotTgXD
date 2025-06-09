package com.example.reminderbot.sheduler;

import com.example.reminderbot.model.NotificationTask;
import com.example.reminderbot.repository.NotificationTaskRepository;
import com.example.reminderbot.service.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component

public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    @Autowired
    private NotificationTaskRepository taskRepository;

    @Autowired
    private TelegramBot telegramBot;

    // Запускаем каждую минуту (в 0-ю секунду)
    @Scheduled(cron = "0 * * * * *")
    public void sendNotifications() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Запуск проверки напоминаний в {}", now);

        List<NotificationTask> tasksToSend = taskRepository.findAllByNotificationDateTimeLessThanEqual(now);

        if (!tasksToSend.isEmpty()) {
            log.info("Найдено {} задач для отправки", tasksToSend.size());
        }

        for (NotificationTask task : tasksToSend) {
            telegramBot.sendMessage(task.getChatId(), "🔔 Напоминание: " + task.getMessage());
            taskRepository.delete(task); // Удаляем задачу после отправки
            log.info("Отправлено и удалено напоминание: {}", task);
        }
    }
}