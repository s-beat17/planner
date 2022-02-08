package ru.javabegin.springboot.business.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor

// возможные значения, по которым можно искать приоритеты
public class PrioritySearchValues {

    private String title; // такое же название должно быть у объекта на frontend
    private String email; // для фильтрации значений конкретного пользователя

    // можно добавлять любые поля, по которых хотите искать



}
