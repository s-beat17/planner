package ru.javabegin.springboot.business.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javabegin.springboot.auth.entity.User;

import javax.persistence.*;

@Entity
@EqualsAndHashCode
@NoArgsConstructor
@Setter
@Getter
public class Category {

    // указываем, что поле заполняется в БД
    // нужно, когда добавляем новый объект и он возвращается уже с новым id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column
    private String title;

    @Column(name = "completed_count", updatable = false) // т.к. это поле высчитывается автоматически в триггерах - вручную его не обновляем (updatable = false)
    private Long completedCount;

    @Column(name = "uncompleted_count", updatable = false) // т.к. это поле высчитывается автоматически в триггерах - вручную его не обновляем (updatable = false)
    private Long uncompletedCount;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id") // по каким полям связывать (foreign key)
    private User user;

    // не создаем обратную ссылку на Task с типом Collection, чтобы каждый раз не тянуть с объектом целую коллекцию - будет перегруз ненужных данных или зацикливание



    /*
        Рекомендую всегда указывать аннотацию @Column, даже если у него нет параметров. Для того, чтобы Hibernate однозначно понимал какое поле связать со столбцом таблицы.
        Во многих кодах можете увидеть, что эта аннотация не указывается и Hibernate сам пытается по названию поля найти соотв. столбец в таблице.

        Аннотацию @Basic, если нет параметров, можно не указывать, она является вспомогательной

     */
}
