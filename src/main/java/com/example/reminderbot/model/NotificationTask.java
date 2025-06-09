package com.example.reminderbot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;


import java.time.LocalDateTime;

@Entity(name = "notification_tasks")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId; // ID чата, куда отправлять напоминание
    private String message; // Текст напоминания
    private LocalDateTime notificationDateTime; // Дата и время отправки

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getNotificationDateTime() { return notificationDateTime; }
    public void setNotificationDateTime(LocalDateTime notificationDateTime) { this.notificationDateTime = notificationDateTime; }
}