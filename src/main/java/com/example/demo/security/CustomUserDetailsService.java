package com.example.demo.security;

import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
// Адаптер между таблицей пользователей и Spring Security. По имени пользователя он собирает объект, который понимает механизм входа.
public class CustomUserDetailsService implements UserDetailsService {

    // Зависимость из подсистемы безопасности. Без неё нельзя проверить пользователя, пароль или токен.
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    // Ищет пользователя по имени и превращает его в объект, который понимает Spring Security.
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "В " + username
                ));
    }
}
