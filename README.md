### Схема взаимодействия ReminderBot (ИСПРАВЛЕНО)

Вот диаграмма последовательности, описывающая ключевые сценарии работы бота:

```mermaid
sequenceDiagram
    %% --- Определяем псевдонимы (alias) ---
    participant User as Пользователь
    participant TgAPI as Telegram API
    participant Bot as TelegramBot
    participant Repo as NotificationTaskRepository
    participant DB as База данных
    participant Scheduler as NotificationScheduler
     %% ---------------------------------------

    box rgb(240, 240, 255) Сценарий 1: Создание напоминания
        User->>TgAPI: 1. Отправляет команду /remind Сделать отчет через 1 hour
        TgAPI->>Bot: 2. Передает Update в onUpdateReceived()
        Bot->>Bot: 3. Вызывает handleRemindCommand(text)
        Bot->>Bot: 4. Распознает команду и вычисляет время
        Bot->>Repo: 5. save(newTask)
        Repo->>DB: 6. INSERT INTO notification_tasks(...)
        DB-->>Repo: 7. Подтверждает сохранение
        Repo-->>Bot: 8. Возвращает сохраненный объект Task
        Bot->>TgAPI: 9. sendMessage("Хорошо, я напомню...")
        TgAPI->>User: 10. Отображает сообщение с подтверждением
    end

    box rgb(255, 240, 240) Сценарий 2: Отправка напоминания (фоновый процесс)
        loop Каждую минуту
            Scheduler->>Scheduler: 11. Запускается по cron (метод sendNotifications)
            Scheduler->>Repo: 12. findAllByNotificationDateTimeLessThanEqual(now)
            Repo->>DB: 13. SELECT * FROM notification_tasks WHERE notification_datetime <= NOW()
            DB-->>Repo: 14. Возвращает список задач (List<Task>)
            Repo-->>Scheduler: 15. Передает список задач для отправки

            alt Если есть задачи для отправки
                Scheduler->>Bot: 16. sendMessage(task.getChatId(), task.getMessage())
                Bot->>TgAPI: 17. Отправляет сообщение "🔔 Напоминание: Сделать отчет"
                TgAPI->>User: 18. Отображает напоминание
                Scheduler->>Repo: 19. delete(task)
                Repo->>DB: 20. DELETE FROM notification_tasks WHERE id = ...
             end
        end
    end
```
