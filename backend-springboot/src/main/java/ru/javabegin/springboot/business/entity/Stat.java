package ru.javabegin.springboot.business.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javabegin.springboot.auth.entity.User;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class Stat { // в этой таблице всего 1 запись, которая обновляется (но никогда не удаляется)

    @Id
    @Column
    private Long id;

    @Column(name = "completed_total", updatable = false)
    private Long completedTotal;

    @Column(name = "uncompleted_total", updatable = false)
    private Long uncompletedTotal;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id") // по каким полям связывать (foreign key)
    public User user;


    /*
        Рекомендую всегда указывать аннотацию @Column, даже если у него нет параметров. Для того, чтобы Hibernate однозначно понимал какое поле связать со столбцом таблицы.
        Во многих кодах можете увидеть, что эта аннотация не указывается и Hibernate сам пытается по названию поля найти соотв. столбец в таблице.

        Аннотацию @Basic, если нет параметров, можно не указывать, она является вспомогательной

     */

}
