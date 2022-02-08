package ru.javabegin.springboot.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.javabegin.springboot.auth.filter.AuthTokenFilter;
import ru.javabegin.springboot.auth.filter.ExceptionHandlerFilter;
import ru.javabegin.springboot.auth.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity(debug = true) // указывает Spring контейнеру, чтобы находил файл конфигурации в классе. debug = true - для просмотра лога какие бины были созданы, в production нужно ставить false
@EnableGlobalMethodSecurity(
        prePostEnabled = true // true означает, что можно будет использовать аннотации pre/post в компонентах Spring (например, @PreAuthorize для доступа к методам или контроллеру только для нужных прав)
)
@EnableAsync
public class SpringConfig extends WebSecurityConfigurerAdapter {

    @Value("${client.url}")
    private String clientURL; // клиентский URL

    // для получения пользователя из БД
    private UserDetailsServiceImpl userDetailsService;

    // перехватывает все выходящие запросы (проверяет jwt если необходимо, автоматически логинит пользователя)
    private AuthTokenFilter authTokenFilter; // его нужно зарегистрировать в filterchain
    private ExceptionHandlerFilter exceptionHandlerFilter; // самый верхний фильтр, который отлавливает ошибки во всех следующих фильтрах и отправляет клиенту в формате JSON

    @Autowired
    public void setExceptionHandlerFilter(ExceptionHandlerFilter exceptionHandlerFilter) {
        this.exceptionHandlerFilter = exceptionHandlerFilter;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsServiceImpl userDetailsService) { // внедряем наш компонент Spring @Service
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setAuthTokenFilter(AuthTokenFilter authTokenFilter) { // внедряем фильтр
        this.authTokenFilter = authTokenFilter;
    }


    /*     С помощью WebMvcConfigurer можно задать глобальные правила CORS сразу для всех контроллеров/методов.
            https://developer.mozilla.org/ru/docs/Web/HTTP/CORS
              Если необходимо - в каждом контроллере можно задавать свои настройки
      */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.
                        addMapping("/**"). // для всех URL
                        allowedOrigins(clientURL). // с каких адресов разрешать запросы (можно указывать через запятую)
                        allowCredentials(true). // разрешить отправлять куки для межсайтового запроса
                        allowedHeaders("*"). // разрешить все заголовки - без этой настройки в некоторых браузерах может не работать
                        allowedMethods("*"); // все методы разрешены (GET,POST и пр.) - без этой настройки CORS не будет работать!
            }
        };
    }



    // кодировщик паролей односторонним алгоритмом хэширования BCrypt https://ru.bitcoinwiki.org/wiki/Bcrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // используем стандартный готовый authenticationManager из Spring контейнера (используется для проверки логина-пароля)
    // эти методы доступны в документации Spring Security - оттуда их можно копировать, чтобы не писать вручную
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // указываем наш сервис userDetailsService для проверки пользователя в БД и кодировщик паролей
    // эти методы доступны в документации Spring Security - оттуда их можно копировать, чтобы не писать вручную
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception { // настройки AuthenticationManager для правильной проверки логин-пароль
        authenticationManagerBuilder.
                userDetailsService(userDetailsService). // использовать наш сервис для загрузки User из БД
                passwordEncoder(passwordEncoder()); // указываем, что используется кодировщик пароля (для корректной проверки пароля)
    }


    // нужно отключить вызов фильтра AuthTokenFilter для сервлет контейнера (чтобы фильтр вызывался не 2 раза, а только один раз из Spring контейнера)
    // https://stackoverflow.com/questions/39314176/filter-invoke-twice-when-register-as-spring-bean
    @Bean
    public FilterRegistrationBean registration(AuthTokenFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter); // FilterRegistrationBean - регистратор фильтров для сервлет контейнера
        registration.setEnabled(false); // отключить исп-е фильтра для сервлет контейнера
        return registration;
    }


    /*     С помощью WebMvcConfigurer можно задать глобальные правила CORS сразу для всех контроллеров/методов.
        Если необходимо - в каждом контроллере можно задавать свои настройки
*/






    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // порядок следования настроек внутри метода - неважен, можно в любой последовательности



        /*
            Отключаем хранение сессии на сервере, в этом нет необходимости.
            Клиент будет просто вызывать RESTful API сервера и передавать токен с информацией о пользователе.
         */
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.csrf().disable(); // выключаем встроенную Spring-защиту от CSRF атак, т.к. будем использовать JWT. Если не отключить, то Spring Security будет пытаться в каждом входящем запроcе искать спец. токен CSRF и выдавать ошибку, если не найдет

        http.formLogin().disable(); // отключаем, не нужна для RESTful API, т.к. форма авторизации создается не на SpringMVC, а на SPA или других технологиях
        http.httpBasic().disable(); // отключаем стандартную браузерную форму авторизации, не нужна, т.к. форма авторизации будет на клиенте

        http.requiresChannel().anyRequest().requiresSecure(); // обязательное исп. HTTPS для всех запросах

        // authTokenFilter - валидация JWT, до того, как запрос попадет в контроллер
        http.addFilterBefore(authTokenFilter, SessionManagementFilter.class); // добавляем наш фильтр в securityfilterchain

//        // отлавливает ошибки последующих фильтром и отправляет их клиенту в формате JSON
        http.addFilterBefore(exceptionHandlerFilter, AuthTokenFilter.class); // этот фильтр должен обязательно находиться перед всеми нашими остальными фильтрами



    }

}
