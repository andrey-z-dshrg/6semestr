package com.example.demo;

import com.example.demo.config.MinioStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MinioStorageProperties.class)
// Точка входа в приложение. Spring Boot стартует именно отсюда и затем поднимает все контроллеры, сервисы, настройки безопасности и подключение к базе.
public class DemoApplication {

    // Отдельный шаг логики внутри класса. Он решает одну локальную задачу и используется из других методов этого же класса.
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
