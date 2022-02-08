package ru.javabegin.springboot.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.javabegin.springboot.business.entity.Priority;

import java.util.List;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {

    // поиск значений по названию для конкретного пользователя
    @Query("SELECT c FROM Priority c where " +

            "(:title is null or :title='' " + // если передадим параметр title пустым, то выберутся все записи (сработает именно это условие)
            " or  lower(c.title) like lower(concat('%', :title, '%'))) " + // если параметр title не пустой, то выполниться уже это условие

            " and c.user.email=:email " + // фильтрация для конкретного пользователя

            "order by c.title asc") // сортировка по названию
    List<Priority> find(@Param("title") String title, @Param("email") String email);

    // поиск категорий пользователя (по email)
    List<Priority> findByUserEmailOrderByIdAsc(String email); // сортировка по порядку вставки в таблицу
}
