package com.example.reminderbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Включаем поддержку @Scheduled
public class ReminderBotApplication {
	public static void main(String[] args) {
		SpringApplication.run(ReminderBotApplication.class, args);
	}
}