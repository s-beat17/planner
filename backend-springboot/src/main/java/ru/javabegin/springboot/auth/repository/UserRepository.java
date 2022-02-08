package ru.javabegin.springboot.auth.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javabegin.springboot.auth.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {


    // для проверки существования email или username возвращаем только true или false (не возвращаем весь объект User, нет смысла)
    @Query("select case when count(u)> 0 then true else false end from User u where lower(u.email) = lower(:email)")
    boolean existsByEmail(@Param("email") String email);

    @Query("select case when count(u)> 0 then true else false end from User u where lower(u.username) = lower(:username)")
    boolean existsByUsername(@Param("username") String username);

    // используем обертку Optional - контейнер, который хранит значение или null - позволяет избежать ошибки NullPointerException
    Optional<User> findByUsername(String username); // поиск по username

    // используем обертку Optional - контейнер, который хранит значение или null - позволяет избежать ошибки NullPointerException
    Optional<User> findByEmail(String email); // поиск по email

    // обновление пароля
    @Modifying // если запрос изменяет данные - желательно добавлять эту аннотацию
    @Transactional // если запрос изменяет данные - желательно добавлять эту аннотацию
    @Query("UPDATE User u SET u.password = :password WHERE u.email=:email") // обновление по email
    int updatePassword(@Param("password") String password, @Param("email") String email); // возвращает int (сколько записей обновил) - в данном случае всегда должен возвращать 1



}
