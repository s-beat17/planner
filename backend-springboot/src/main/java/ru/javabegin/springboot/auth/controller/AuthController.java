package ru.javabegin.springboot.auth.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.springboot.auth.entity.Activity;
import ru.javabegin.springboot.auth.entity.Role;
import ru.javabegin.springboot.auth.entity.User;
import ru.javabegin.springboot.auth.exception.RoleNotFoundException;
import ru.javabegin.springboot.auth.exception.UserAlreadyActivatedException;
import ru.javabegin.springboot.auth.exception.UserOrEmailExistsException;
import ru.javabegin.springboot.auth.objects.JsonException;
import ru.javabegin.springboot.auth.service.EmailService;
import ru.javabegin.springboot.auth.service.UserDetailsImpl;
import ru.javabegin.springboot.auth.service.UserDetailsServiceImpl;
import ru.javabegin.springboot.auth.service.UserService;
import ru.javabegin.springboot.auth.utils.CookieUtils;
import ru.javabegin.springboot.auth.utils.JwtUtils;

import javax.validation.Valid;
import java.util.UUID;

import static ru.javabegin.springboot.auth.service.UserService.DEFAULT_ROLE;

@RestController
@RequestMapping("/auth")
@Log
public class AuthController {

    private UserService userService; // сервис для работы с пользователями
    private PasswordEncoder encoder; // кодировщик паролей (или любых данных), создает односторонний хеш
    private AuthenticationManager authenticationManager; // стандартный встроенный менеджер Spring, проверяет логин-пароль
    private JwtUtils jwtUtils; // класс-утилита для работы с jwt
    private CookieUtils cookieUtils; // класс-утилита для работы с куками
    private EmailService emailService; // сервис отправки писем
    private UserDetailsServiceImpl userDetailsService; // для поиска пользователя и добавления его в Spring контейнер




    @Autowired
    public AuthController(UserService userService, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils, CookieUtils cookieUtils, EmailService emailService, UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.cookieUtils = cookieUtils;
        this.emailService = emailService;
        this.userDetailsService = userDetailsService;

    }



    @PostMapping("/test-no-auth")
    public String testNoAuth() {
        return "OK-no-auth";
    }

    @PostMapping("/test-with-auth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String testWithAuth() {
        return "OK-with-auth";
    }



    // выход из системы - мы должны занулить (удалить) кук с jwt (пользователю придется заново логиниться при след. входе)
    @PostMapping("/logout")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity logout() { // body отсутствует (ничего не передаем от клиента) - все данные пользователя передаются с куком


        // главная задача при logout - это удалить кук

        // создаем кук с истекшим сроком действия, тем самым браузер удалит такой кук автоматически
        HttpCookie cookie = cookieUtils.deleteJwtCookie();

        HttpHeaders responseHeaders = new HttpHeaders(); // объект для добавления заголовков в response
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString()); // добавляем кук в заголовок (header)

        // в теле запроса ничего не отправляем, отправляем только кук обратно клиенту (браузер автоматически удалит его)
        return ResponseEntity.ok().headers(responseHeaders).build();

    }



    // регистрация нового пользователя
    // этот метод всем будет доступен для вызова (не будем его "защищать" с помощью токенов, т.к. это не требуется по задаче)
    @PutMapping("/register")
    public ResponseEntity register(@Valid @RequestBody User user) { // здесь параметр user используется, чтобы передать все данные пользователя для регистрации

        // если имя или email уже существуют в БД - не даем создать пользователя
        if (userService.userExists(user.getUsername(), user.getEmail())) {
            throw new UserOrEmailExistsException("User or email already exists"); // возвращаем клиенту ошибку
        }


        // присваиваем дефолтную роль новому пользователю

        Role userRole = userService.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new RoleNotFoundException("Default Role USER not found.")); // если в БД нет такой роли - выбрасываем исключение
        user.getRoles().add(userRole); // добавить роль USER для создаваемого пользователя

        /* нам не нужно создавать новый объект Role, а нужно получать его из БД (т.к. все роли уже созданы в таблицах)
           Объект роли нужно всегда получать из БД перед регистрацией пользователя, т.к. все должно работать динамически (если роли изменили в БД, мы считаем обновленные данные)
          Т.е. вариант загрузки всех ролей в начале работы приложения - не подойдет, т.к. данные в БД могут измениться, а нас останется неактуальные роли
        */



        // зашифровать пароль (алгоритм BCrypt)
        user.setPassword(encoder.encode(user.getPassword())); // генерим хеш пароля на основе переданного текста


        // сохранить в БД активность пользователя
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setUuid(UUID.randomUUID().toString()); // уникальный UUID - нужен для активации пользователя

        userService.register(user, activity);

        // отправляем письмо о том, что нужно активировать аккаунт (выполняется в параллельном потоке с помощью @Async, чтобы пользователь не ждал)
        emailService.sendActivationEmail(user.getEmail(), user.getUsername(), activity.getUuid());

        return ResponseEntity.ok().build(); // просто отправляем статус 200-ОК (без каких-либо данных) - значит регистрация выполнилась успешно
    }


    // активация пользователя (чтобы мог авторизоваться и работать дальше с приложением) - не требует авторизации для вызова
    // этот метод всем будет доступен для вызова (не будем его "защищать" с помощью токенов, т.к. это не требуется по задаче)
    @PostMapping("/activate-account")
    public ResponseEntity<Boolean> activateUser(@RequestBody String uuid) { // true - успешно активирован

        // проверяем UUID пользователя, которого хотим активировать
        Activity activity = userService.findActivityByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("Activity Not Found with uuid: " + uuid));

        // если пользователь уже был ранее активирован
        if (activity.isActivated())
            throw new UserAlreadyActivatedException("User already activated");

        // возвращает кол-во обновленных записей (в нашем случае должна быть 1)
        int updatedCount = userService.activate(uuid); // активируем пользователя

        return ResponseEntity.ok(updatedCount == 1); // 1 - значит запись обновилась успешно, 0 - что-то пошло не так
    }





    // залогиниться по паролю-пользователю - не требует авторизации для вызова
    // этот метод всем будет доступен для вызова (не будем его "защищать" с помощью токенов, т.к. это не требуется по задаче)
    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody User user) { // здесь параметр user используется как контейнер, чтобы передать логин-пароль

        // проверяем логин-пароль
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        // добавляем в Spring-контейнер информацию об авторизации (чтобы Spring понимал, что пользователь успешно вошел и мог использовать его роли и другие параметры)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // UserDetailsImpl - спец. объект, который хранится в Spring контейнере и содержит данные пользователя
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // активирован пользователь или нет (проверяем только после того, как пользователь успешно залогинился)
        if (!userDetails.isActivated()) {
            throw new DisabledException("User disabled"); // клиенту отправится ошибка о том, что пользователь не активирован
        }


        // если мы дошло до этой строки, значит пользователь успешно залогинился


        // после каждого успешного входа генерируется новый jwt, чтобы следующие запросы на backend авторизовывать автоматически
        String jwt = jwtUtils.createAccessToken(userDetails.getUser());


        // создаем кук со значением jwt (браузер будет отправлять его автоматически на backend при каждом запросе)
        // обратите внимание на флаги безопасности в методе создания кука
        HttpCookie cookie = cookieUtils.createJwtCookie(jwt); // server-side cookie

        HttpHeaders responseHeaders = new HttpHeaders(); // объект для добавления заголовков в response
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString()); // добавляем server-side cookie в заголовок (header)


        // отправляем клиенту данные пользователя (и jwt-кук в заголовке Set-Cookie)
        return ResponseEntity.ok().headers(responseHeaders).body(userDetails.getUser());


    }


    // повторная отправка письма для активации аккаунта - это уже инициирует пользователь лично
    @PostMapping("/resend-activate-email")
    public ResponseEntity resendActivateEmail(@RequestBody String usernameOrEmail) {

        // находим пользователя в БД (ищет как по email, так и по username)
        UserDetailsImpl user = (UserDetailsImpl) userDetailsService.loadUserByUsername(usernameOrEmail); // смотрим, есть ли такой пользователь в базе (ищет сначала по username, затем по email)

        // у каждого пользователя должна быть запись Activity (вся его активность) - если этого объекта нет - значит что-то пошло не так
        Activity activity = userService.findActivityByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Activity Not Found with user: " + usernameOrEmail));

        // если пользователь уже был ранее активирован (нет смысла еще раз делать запрос в БД)
        if (activity.isActivated())
            throw new UserAlreadyActivatedException("User already activated: " + usernameOrEmail);

        // отправляем письмо активации (выполняется в параллельном потоке с помощью @Async, чтобы пользователь не ждал)
        emailService.sendActivationEmail(user.getEmail(), user.getUsername(), activity.getUuid());

        return ResponseEntity.ok().build(); // просто отправляем статус 200-ОК (без каких-либо данных)
    }


    // отправка письма для сброса пароля
    @PostMapping("/send-reset-password-email")
    public ResponseEntity sendEmailResetPassword(@RequestBody String email) {

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email); // смотрим, есть ли такой пользователь в БД - иначе выбросит ошибку

        User user = userDetails.getUser(); // получаем текущего пользователя из контейнера UserDetails

        if (userDetails != null) {
            // отправляем письмо со ссылкой для сброса пароля (выполняется в параллельном потоке с помощью @Async, чтобы пользователь не ждал)
            emailService.sendResetPasswordEmail(user.getEmail(), jwtUtils.createEmailResetToken(user));
        }

        return ResponseEntity.ok().build(); // во всех случаях просто возвращаем статус 200 - ОК
    }




    // обновление пароля (когда клиент ввел новый пароль и отправил его на backend) - не требует авторизации для вызова
    @PostMapping("/update-password")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Boolean> updatePassword(@RequestBody String password) { // password - новый пароль

         /*
            До этого шага должна была произойти автоматическая авторизация пользователя в AuthTokenFilter на основе кука jwt.
            Без этой информации метод не выполнится, т.к. не сможем получить пользователя, для которого хотим выполнить операцию
         */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // пытаемся получить объект аутентификации
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal(); // получаем пользователя из Spring контейнера

        // кол-во обновленных записей (в нашем случае должно быть 1, т.к. обновляем пароль одного пользователя)
        int updatedCount = userService.updatePassword(encoder.encode(password), user.getEmail()); // для обновления пароля нужно знать только email

        return ResponseEntity.ok(updatedCount == 1); // 1 - значит запись обновилась успешно, 0 - что-то пошло не так
    }



    /*

    Метод перехватывает все ошибки в контроллере (неверный логин-пароль и пр.)

    Даже без этого метода все ошибки будут отправляться клиенту, просто здесь это можно кастомизировать, например отправить JSON в нужном формате

    Можно настроить, какие типа ошибок отправлять в явном виде, а какие нет (чтобы не давать лишнюю информацию злоумышленникам)

    */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonException> handleExceptions(Exception ex) {

/*

        DisabledException (наш созданный класс) - не активирован
        UserAlreadyActivatedException - пользователь уже активирован (пытается неск. раз активировать)
        UsernameNotFoundException - username или email не найден в базе

        BadCredentialsException - неверный логин-пароль (или любые другие данные пользователя, которые он использовал для аутентификации)
        UserOrEmailExistsException - пользователь или email уже существуют
        DataIntegrityViolationException - ошибка уникальности в БД

        Эти типы ошибок можно будет считывать на клиенте и обрабатывать как нужно (например, показать текст ошибки)

*/

        // отправляем название класса ошибки (чтобы правильно обработать ошибку на клиенте)
        return new ResponseEntity(new JsonException(ex.getClass().getSimpleName()), HttpStatus.BAD_REQUEST); // Spring автоматически конвертирует объект JsonException в JSON

    }

}
