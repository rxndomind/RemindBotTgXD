package com.example.reminderbot.repository;

import com.example.reminderbot.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findAllByNotificationDateTimeLessThanEqual(LocalDateTime dateTime);

    List<NotificationTask> findByChatIdAndNotificationDateTimeAfter(Long chatId, LocalDateTime dateTime);
}