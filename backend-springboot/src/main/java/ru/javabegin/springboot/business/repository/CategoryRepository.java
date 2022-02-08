package ru.javabegin.springboot.business.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.javabegin.springboot.business.entity.Category;

import java.util.List;

// реализует все необходимые запросы в БД

/*

При запуске проекта Spring  находит все переменные, которые имеют тип этого интерфейса.

После этого внедряет в эти переменные нужные объекты, где уже реализованы все методы.

 */

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // поиск значений по названию для конкретного пользователя
    @Query("SELECT c FROM Category c where " +

            "(:title is null or :title='' " + // если передадим параметр title пустым, то выберутся все записи (сработает именно это условие)
            " or  lower(c.title) like lower(concat('%', :title, '%'))) " + // если параметр title не пустой, то выполниться уже это условие

            " and c.user.email=:email " + // фильтрация для конкретного пользователя

            "order by c.title asc") // сортировка по названию
    List<Category> findByTitle(@Param("title") String title, @Param("email") String email);

    // поиск категорий пользователя (по email)
    List<Category> findByUserEmailOrderByTitleAsc(String email);

}
