sequenceDiagram
    participant Пользователь
    participant Telegram API
    participant TelegramBot
    participant NotificationTaskRepository
    participant База данных
    participant NotificationScheduler

    box rgb(240, 240, 255) Сценарий 1: Создание напоминания
        Пользователь->>Telegram API: Отправляет команду /remind Сделать отчет через 1 hour
        Telegram API->>TelegramBot: Передает Update в onUpdateReceived()
        TelegramBot->>TelegramBot: Вызывает handleRemindCommand(text)
        TelegramBot->>TelegramBot: Распознает команду и вычисляет время
        TelegramBot->>NotificationTaskRepository: save(newTask)
        NotificationTaskRepository->>База данных: INSERT INTO notification_tasks(...)
        База данных-->>NotificationTaskRepository: Подтверждает сохранение
        NotificationTaskRepository-->>TelegramBot: Возвращает сохраненный объект Task
        TelegramBot->>Telegram API: sendMessage("Хорошо, я напомню...")
        Telegram API->>Пользователь: Отображает сообщение с подтверждением
    end

    box rgb(255, 240, 240) Сценарий 2: Отправка напоминания (фоновый процесс)
        loop Каждую минуту
            NotificationScheduler->>NotificationScheduler: Запускается по cron (метод sendNotifications)
            NotificationScheduler->>NotificationTaskRepository: findAllByNotificationDateTimeLessThanEqual(now)
            NotificationTaskRepository->>База данных: SELECT * FROM notification_tasks WHERE notification_datetime <= NOW()
            База данных-->>NotificationTaskRepository: Возвращает список задач (List<Task>)
            NotificationTaskRepository->>NotificationScheduler: Передает список задач для отправки

            alt Если есть задачи для отправки
                NotificationScheduler->>TelegramBot: sendMessage(task.getChatId(), task.getMessage())
                TelegramBot->>Telegram API: Отправляет сообщение "🔔 Напоминание: Сделать отчет"
                Telegram API->>Пользователь: Отображает напоминание
                NotificationScheduler->>NotificationTaskRepository: delete(task)
                NotificationTaskRepository->>База данных: DELETE FROM notification_tasks WHERE id = ...
            end
        end
    end
