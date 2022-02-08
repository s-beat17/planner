package ru.javabegin.springboot.auth.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/*

Пользователь со своими данными, правами.

Важно то, что объект не имеет прямых связей с другими объектами бизнес-процесса.

User только хранит информацию о пользователе, а как его будут использовать, ему не важно.


 */

@Setter
@Getter
@Entity
@Table(name="USER_DATA") // явно указываем название таблицы, если оно отличается от названия класса с маленькой буквы
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username; // имя пользователя (аккаунта)

    // обратная ссылка - указываем поле "user" из Activity, которое ссылается на User
    // Activity имеет внешний ключ на User
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    public Activity activity; // действия пользователя (активация и любые другие)


    @Email // встроенный валидатор на правильное написание email
    @Column
    private String email;

    @Column
    private String password; // пароль желательно занулять сразу после аутентификации (в контроллере), чтобы он нигде больше не "светился"


    @ManyToMany(fetch = FetchType.LAZY) // таблица role ссылается на user через промежуточную таблицу user_role
    @JoinTable(	name = "USER_ROLE",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();




    // для сравнения объектов User между собой (если email равны - значит объекты тоже равны)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return email.equals(user.email); // сравнение объектов по email
    }

    // обязательно нужно реализовывать, если реализован equals
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}

