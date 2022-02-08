import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AuthService, User} from '../service/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  form: FormGroup; // форма с введенными пользователем данными
  user: User; // данные пользователя после того, как успешно залогинится
  error: string; // текст ошибки (если есть) - возвращается от backend
  firstSubmitted = false; // становится true при первом нажатии (чтобы сразу не показывать ошибки полей, а только после нажатия Войти)
  isLoading = false; // идет ли загрузка в данный момент (для показа/скрытия индикатора загрузки)



  // внедрение всех нужных объектов
  constructor(
    private formBuilder: FormBuilder, // для создание формы
    private authService: AuthService // сервис аутентификации
  ) {
  }

  // автоматически вызывается при создании компонента LoginComponent
  ngOnInit(): void {
    // инициализация формы с нужными полями, начальными значениями и валидаторами
    this.form = this.formBuilder.group({
        username: ['', [Validators.required, Validators.minLength(6)]], // Validators.required - проверка на обязательность заполнения
        password: ['', [Validators.required, Validators.minLength(6)]],
      }
    );

  }

  // быстрый доступ на компоненты формы (для сокращения кода, чтобы каждый раз не писать this.form.get('') )
  get usernameField(): AbstractControl {
    return this.form.get('username');
  }

  get passwordField(): AbstractControl {
    return this.form.get('password');
  }


  // попытка отправки данных формы аутентификации
  public submitForm(): void {

    this.firstSubmitted = true; // один раз нажали на отправку формы (можно теперь показывать ошибки)

    if (this.form.invalid) { // если есть хотя бы одна ошибка в введенных данных формы
      return; // не отправляем данные на сервер
    }

    this.isLoading = true; // отображает индикатор загрузки

    // объект для отправки на сервер (добавляется в тело запроса)
    const tmpUser = new User();
    tmpUser.username = this.usernameField.value; // берем введенное значение пользователя
    tmpUser.password = this.passwordField.value;


    // отправка запроса на сервер
    this.authService.login(tmpUser).subscribe( // подписываемся на результат работы backend
      result => { // запрос успешно выполнился без ошибок (значит пользователь ввел верные логин-пароль)

        this.isLoading = false; // скрывает индикатор загрузки


        /*
        Пароль передается только 1 раз при аутентификации и не сохраняется в jwt.
        При каждой успешной аутентификации - на бэкенде генерируется новый jwt
         */


        // примечание:
        // - данные пользователя находятся не внутри jwt (payload), а просто в составе JSON (не закодированное поле user)
        // - внутри jwt у нас только username и другие системные значения по необходимости (expiration и пр.)

        this.user = result; // получаем пользователя из JSON и сохраняем в память приложения (в переменную)


        console.log('user = ' + this.user);


      },
      err => { // запрос выполнился с ошибкой (можем использовать переменную err)

        this.isLoading = false; // скрывает индикатор загрузки


        switch (err.error.exception) { // считываем тип ошибки, чтобы правильно среагировать

          case 'BadCredentialsException': { // пользователь неверно ввел логин-пароль
            this.error = `Ошибка: проверьте логин или пароль.`; // будет показана ошибка на странице
            break;
          }

          case 'DisabledException': { // пользователь верно ввел логин-пароль, но его пользователь еще не активирован
            this.error = `Пользователь не активирован`; // будет показана ошибка на странице
            break;
          }


          default: { // если любой другой тип ошибки - просто показать информацию
            this.error = `Ошибка (обратитесь к администратору)`; // будет показана ошибка на странице
            break;
          }

        }


      }
    );


  }


}
