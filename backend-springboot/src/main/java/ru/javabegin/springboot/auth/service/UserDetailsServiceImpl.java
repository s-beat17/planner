package ru.javabegin.springboot.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.javabegin.springboot.auth.entity.User;
import ru.javabegin.springboot.auth.repository.UserRepository;

import java.util.Optional;

/*

Сервис, который используется для проверки пользователя в БД при аутентификации/авторизации пользователя (логин-пароль)

Метод loadUserByUsername автоматически вызывается Spring контейнером (когда пытаемся залогинить пользователя методом authenticate), чтобы найти пользователя в БД.
Затем Spring сравнивает хэши паролей (введенного и фактического) и выдает результат (все ок или выбрасывает исключение, которое можно оправить клиенту)

Также, метод loadUserByUsername можно вызывать самим, вручную, когда необходимо проверить наличие пользователя в БД (по username или email).

Чтобы этот класс был задействован в аутентификации - его нужно указать в Spring настройках в методе configure(AuthenticationManagerBuilder authenticationManagerBuilder)

Класс обязательно должен реализовать интерфейс UserDetailsService, чтобы Spring "принимал" этот класс.


 */

@Service
public class UserDetailsServiceImpl implements UserDetailsService { // Impl в названии класса означает "Implementation" - реализация

    private UserRepository userRepository; // доступ к БД

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    // метод ищет пользователя по username или email (любое совпадение)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // этот метод используется при аутентификации пользователя

        // используем обертку Optional - контейнер, который хранит значение или null - позволяет избежать ошибки NullPointerException
        Optional<User> userOptional = userRepository.findByUsername(username); // сначала пытаемся найти по имени

        if (!userOptional.isPresent()) { // если не нашли по имени
            userOptional = userRepository.findByEmail(username); // пытаемся найти по email
        }

        if (!userOptional.isPresent()) { // если не нашли ни по имени, ни по email
            throw new UsernameNotFoundException("User Not Found with username or email: " + username); // выбрасываем исключение, которое можно отправить клиенту
        }

        return new UserDetailsImpl(userOptional.get()); // если пользователь в БД найден - создаем объект UserDetailsImpl (с объектом User внутри), который потом будет добавлен в Spring контейнер и в объект Principal
    }


//    @Transactional
//    // метод ищет пользователя по username или email (любое совпадение)
//    public UserDetails loadUserById(Long id) throws UsernameNotFoundException { // этот метод используется при аутентификации пользователя
//
//        // используем обертку Optional - контейнер, который хранит значение или null - позволяет избежать ошибки NullPointerException
//        Optional<User> userOptional = userRepository.findById(id); // поиск записи по id
//
//        if (!userOptional.isPresent()) { // если не нашли ни по имени, ни по email
//            throw new UsernameNotFoundException("User Not Found with id: " + id); // выбрасываем исключение, которое можно отправить клиенту
//        }
//
//        return new UserDetailsImpl(userOptional.get()); // если пользователь в БД найден - создаем объект UserDetailsImpl (с объектом User внутри), который потом будет добавлен в Spring контейнер
//    }


}

