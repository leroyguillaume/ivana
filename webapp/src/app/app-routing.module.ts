import {NgModule} from '@angular/core'
import {RouterModule, Routes} from '@angular/router'
import {LoginComponent} from './login/login.component'
import {HomeComponent} from './home/home.component'
import {AuthenticatedGuard} from './authenticated.guard'
import {AnonymousGuard} from './anonymous.guard'
import {PhotoComponent} from './photo/photo.component'
import {AdminComponent} from './admin/admin.component'
import {AdminGuard} from './admin.guard'


const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [AnonymousGuard]
  },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthenticatedGuard]
  },
  {
    path: 'photo/:id',
    component: PhotoComponent,
    canActivate: [AuthenticatedGuard]
  },
  {
    path: 'admin',
    pathMatch: 'full',
    component: AdminComponent,
    canActivate: [AdminGuard]
  },
  {
    path: '**',
    redirectTo: 'home'
  }
]

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
