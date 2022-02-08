package ru.javabegin.springboot.business.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.javabegin.springboot.business.entity.Priority;
import ru.javabegin.springboot.business.search.PrioritySearchValues;
import ru.javabegin.springboot.business.service.PriorityService;
import ru.javabegin.springboot.business.util.MyLogger;

import java.util.List;
import java.util.NoSuchElementException;

/*

Чтобы дать меньше шансов для взлома (например, CSRF атак): POST/PUT запросы могут изменять/фильтровать закрытые данные, а GET запросы - для получения незащищенных данных
Т.е. GET-запросы не должны использоваться для изменения/получения секретных данных

Если возникнет exception - вернется код  500 Internal Server Error, поэтому не нужно все действия оборачивать в try-catch

Используем @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON

Названия методов могут быть любыми, главное не дублировать их имена и URL mapping

*/


@RestController
@RequestMapping("/priority") // базовый URI

public class PriorityController {

    // доступ к данным из БД
    private PriorityService priorityService;

    // автоматическое внедрение экземпляра класса через конструктор
    // не используем @Autowired ля переменной класса, т.к. "Field injection is not recommended "
    @Autowired
    public PriorityController(PriorityService priorityService) {
        this.priorityService = priorityService;
    }


    @PostMapping("/all")
    public ResponseEntity<List<Priority>> findAll(@RequestBody String email) {

        MyLogger.debugMethodName("PriorityController: findAll() ---------------------------------------------------------- ");


        return ResponseEntity.ok(priorityService.findAll(email));

    }


    @PutMapping("/add")
    public ResponseEntity<Priority> add(@RequestBody Priority priority) {

        MyLogger.debugMethodName("PriorityController: add() ---------------------------------------------------------- ");


        // проверка на обязательные параметры
        if (priority.getId() != null && priority.getId() != 0) {
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title
        if (priority.getTitle() == null || priority.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение color
        if (priority.getColor() == null || priority.getColor().trim().length() == 0) {
            return new ResponseEntity("missed param: color", HttpStatus.NOT_ACCEPTABLE);
        }

        // save работает как на добавление, так и на обновление
        return ResponseEntity.ok(priorityService.add(priority));
    }


    @PatchMapping("/update")
    public ResponseEntity update(@RequestBody Priority priority) {

        MyLogger.debugMethodName("PriorityController: update() ---------------------------------------------------------- ");


        // проверка на обязательные параметры
        if (priority.getId() == null || priority.getId() == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title
        if (priority.getTitle() == null || priority.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение color
        if (priority.getColor() == null || priority.getColor().trim().length() == 0) {
            return new ResponseEntity("missed param: color", HttpStatus.NOT_ACCEPTABLE);
        }

        // save работает как на добавление, так и на обновление
        priorityService.update(priority);


        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 (операция прошла успешно)

    }

    // параметр id передаются не в BODY запроса, а в самом URL
    @PostMapping("/id")
    public ResponseEntity<Priority> findById(@RequestBody Long id) {

        MyLogger.debugMethodName("PriorityController: findById() ---------------------------------------------------------- ");

        // проверка на обязательные параметры - id должен быть заполнен, т.к. это обновление существующего объекта
        if (id == null || id == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        Priority priority = null;

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            priority = priorityService.findById(id);
        } catch (NoSuchElementException e) { // если объект не будет найден
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(priority);
    }


    // для удаления используем типа запроса put, а не delete, т.к. он позволяет передавать значение в body, а не в адресной строке
    @DeleteMapping("/delete")
    public ResponseEntity delete(@RequestBody Long id) {

        MyLogger.debugMethodName("PriorityController: delete() ---------------------------------------------------------- ");


        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            priorityService.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 (операция прошла успешно)
    }


    // поиск по любым параметрам PrioritySearchValues
    @PostMapping("/search")
    public ResponseEntity<List<Priority>> search(@RequestBody PrioritySearchValues prioritySearchValues) {

        MyLogger.debugMethodName("PriorityController: search() ---------------------------------------------------------- ");


        // если вместо текста будет пусто или null - вернутся все категории
        return ResponseEntity.ok(priorityService.find(prioritySearchValues.getTitle(), prioritySearchValues.getEmail()));
    }


}