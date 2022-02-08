package ru.javabegin.springboot.business.controller;

/*

Чтобы дать меньше шансов для взлома (например, CSRF атак): POST/PUT запросы могут изменять/фильтровать закрытые данные, а GET запросы - для получения незащищенных данных
Т.е. GET-запросы не должны использоваться для изменения/получения секретных данных

Если возникнет exception - вернется код  500 Internal Server Error, поэтому не нужно все действия оборачивать в try-catch

Используем @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON

Названия методов могут быть любыми, главное не дублировать их имена и URL mapping

*/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.javabegin.springboot.business.entity.Stat;
import ru.javabegin.springboot.business.service.StatService;
import ru.javabegin.springboot.business.util.MyLogger;

@RestController

public class StatController {

    private final StatService statService; // сервис для доступа к данным (напрямую к репозиториям не обращаемся)

    // автоматическое внедрение экземпляра класса через конструктор
    // не используем @Autowired ля переменной класса, т.к. "Field injection is not recommended "
    @Autowired
    public StatController(StatService statService) {
        this.statService = statService;
    }


    // для статистики всегда получаем только одну строку для конкретного пользователя
    @PostMapping("/stat")
    public ResponseEntity<Stat> findByEmail(@RequestBody String email) {

        MyLogger.debugMethodName("StatController: findById() ---------------------------------------------------------- ");

        return ResponseEntity.ok(statService.findStat(email));
    }


}