import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './auth/login/login.component';


/*

Модуль для настройки всех роутингов (Routing)

Роутинг связывает URI (ссылки, по которым переходит пользователь) и компоненты (страницы)

При переходе по какому либо адресу - произойдет перенаправление на нужный компонент (страницу)

https://angular.io/guide/router


 */


// список всех роутов и связанных компонентов (маппинг)
const routes: Routes = [

  // страницы для неавторизованных пользователей
  {path: '', component: LoginComponent}, // главная
  {path: 'logout', redirectTo: '', pathMatch: 'full'}, // главная
  {path: 'index', redirectTo: '', pathMatch: 'full'}, // главная

];


@NgModule({
  imports: [
    RouterModule.forRoot(routes) // без этого работать не будет - импортирует системный модуль
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule { }
