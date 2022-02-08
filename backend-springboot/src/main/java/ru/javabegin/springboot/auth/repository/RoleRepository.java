package ru.javabegin.springboot.auth.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.javabegin.springboot.auth.entity.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {


    // возвращает контейнер Optional, в котором может быть объект или null
    Optional<Role> findByName(String name); // поиск роли по названию


}
