package ru.javabegin.springboot.business.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.javabegin.springboot.business.entity.Stat;

// принцип ООП: абстракция-реализация - здесь описываем все доступные способы доступа к данным
@Repository
public interface StatRepository extends CrudRepository<Stat, Long> {
    Stat findByUserEmail(String email); // возвращается только 1 запись (каждый пользователь содержит только 1 запись в таблице Stat)
}
