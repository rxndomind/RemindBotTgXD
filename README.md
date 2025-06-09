### Схема взаимодействия ReminderBot

Вот диаграмма последовательности, описывающая ключевые сценарии работы бота:

```mermaid
sequenceDiagram
    participant Пользователь
    participant Telegram API
    participant TelegramBot
    participant NotificationTaskRepository
    participant База данных
    participant NotificationScheduler

    box rgb(240, 240, 255) Сценарий 1: Создание напоминания
        Пользователь->>Telegram API: 1. Отправляет команду /remind Сделать отчет через 1 hour
        Telegram API->>TelegramBot: 2. Передает Update в onUpdateReceived()
        TelegramBot->>TelegramBot: 3. Вызывает handleRemindCommand(text)
        TelegramBot->>TelegramBot: 4. Распознает команду и вычисляет время
        TelegramBot->>NotificationTaskRepository: 5. save(newTask)
        NotificationTaskRepository->>База данных: 6. INSERT INTO notification_tasks(...)
        База данных-->>NotificationTaskRepository: 7. Подтверждает сохранение
        NotificationTaskRepository-->>TelegramBot: 8. Возвращает сохраненный объект Task
        TelegramBot->>Telegram API: 9. sendMessage("Хорошо, я напомню...")
        Telegram API->>Пользователь: 10. Отображает сообщение с подтверждением
    end

    box rgb(255, 240, 240) Сценарий 2: Отправка напоминания (фоновый процесс)
        loop Каждую минуту
            NotificationScheduler->>NotificationScheduler: 11. Запускается по cron (метод sendNotifications)
            NotificationScheduler->>NotificationTaskRepository: 12. findAllByNotificationDateTimeLessThanEqual(now)
            NotificationTaskRepository->>База данных: 13. SELECT * FROM notification_tasks WHERE notification_datetime <= NOW()
            База данных-->>NotificationTaskRepository: 14. Возвращает список задач (List<Task>)
            NotificationTaskRepository-->>NotificationScheduler: 15. Передает список задач для отправки

            alt Если есть задачи для отправки
                NotificationScheduler->>TelegramBot: 16. sendMessage(task.getChatId(), task.getMessage())
                TelegramBot->>Telegram API: 17. Отправляет сообщение "🔔 Напоминание: Сделать отчет"
                Telegram API->>Пользователь: 18. Отображает напоминание
                NotificationScheduler->>NotificationTaskRepository: 19. delete(task)
                NotificationTaskRepository->>База данных: 20. DELETE FROM notification_tasks WHERE id = ...
            end
        end
    end
```
