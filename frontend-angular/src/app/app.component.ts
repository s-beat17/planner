import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'app-root', // по этому названию можем обращаться к компоненту
  templateUrl: './app.component.html' // какой HTML файл отображать
})
export class AppComponent implements OnInit {

  cookieEnabled: boolean; // будет хранить true или false - включены или отключены куки в браузере

  // метод будем вызываться автоматически при иниц. компонента
  ngOnInit(): void {

    this.cookieEnabled = navigator.cookieEnabled; // проверяем включены ли куки в браузере

    // попробовать установить тестовый кук - если не получится - значит куки не работают
    if (!this.cookieEnabled) { // убеждаемся, что нельзя записать кук
      document.cookie = 'testcookie';
      this.cookieEnabled = (document.cookie.indexOf('testcookie') !== -1); // записываем в переменную true или false
    }

  }
}
