package ru.javabegin.springboot.auth.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/*

Привязанная роль к пользователю (ролей у пользователя может быть несколько)

*/

@Setter
@Getter
@Entity
@Table(name="ROLE_DATA") // явно указываем название таблицы, если оно отличается от названия класса с маленькой буквы
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name; // название роли

}
