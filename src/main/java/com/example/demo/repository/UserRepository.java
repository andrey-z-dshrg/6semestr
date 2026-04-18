package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Репозиторий Spring Data. Через него сервисы читают и сохраняют данные, не прописывая SQL вручную для каждой операции.
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
