package ru.javabegin.springboot.business.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javabegin.springboot.auth.entity.User;

import javax.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class Task {

    // указываем, что поле заполняется в БД
    // нужно, когда добавляем новый объект и он возвращается уже с новым id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column
    private String title;

    @Column
    private Integer completed; // 1 = true, 0 = false

    @Column(name = "task_date") // в БД поле называется task_date, т.к. нельзя использовать системное имя date
    private Date taskDate;

    // ссылка на объект Priority
    // задача может иметь только один приоритет (с обратной стороны - один и тот же приоритет может быть использоваться в множестве задач)
    @ManyToOne
    @JoinColumn(name = "priority_id", referencedColumnName = "id") // по каким полям связывать (foreign key)
    private Priority priority;

    // ссылка на объект Category
    // задача может иметь только одну категорию (с обратной стороны - одна и та же категория может быть использоваться в множестве задач)
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id") // по каким полям связывать (foreign key)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id") // по каким полям связывать (foreign key)
    private User user; // для какого пользователя задача


    /*
        Рекомендую всегда указывать аннотацию @Column, даже если у него нет параметров. Для того, чтобы Hibernate однозначно понимал какое поле связать со столбцом таблицы.
        Во многих кодах можете увидеть, что эта аннотация не указывается и Hibernate сам пытается по названию поля найти соотв. столбец в таблице.

        Аннотацию @Basic, если нет параметров, можно не указывать, она является вспомогательной

     */

}
