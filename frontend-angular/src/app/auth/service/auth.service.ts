import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {Router} from '@angular/router';
import {environment} from '../../../environments/environment';


/*
Cервис для взаимодействия с backend системой по части аутентификации/авторизации

*/

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  backendAuthURI = environment.backendURL + '/auth'; // ссылка на корневой URL бекенда, связанного с авторизацией

  constructor(
    private httpClient: HttpClient // для отправки запросов на backend
  ) {
  }

  // аутентификация
  public login(request: User): Observable<User> { // ajax запрос
    return this.httpClient.post<User>(this.backendAuthURI + '/login', request); // request - это body запроса в формате JSON
  }



}


/*

Классы (объекты) для запросов и их результатов, которые автоматически будут преобразовываться в JSON.

Это аналог entity классов из backend проекта (в упрощенном виде)

Должны совпадать по полям с entity-классами backend!!! (иначе не будет правильно отрабатывать автом. упаковка и распаковка JSON)

*/

// пользователь - хранит свои данные
export class User {
  id: number; // обязательное поле, по нему определяется пользователь
  username: string;
  email: string;
  password: string; // не передается с сервера (только от клиента к серверу, например при обновлении пароля)
  roles: Array<Role>; // USER, ADMIN, MODERATOR
}
// роль пользователя
export class Role {
  name: string;
}


