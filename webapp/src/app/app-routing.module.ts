import {NgModule} from '@angular/core'
import {RouterModule, Routes} from '@angular/router'
import {LoginComponent} from './login/login.component'
import {HomeComponent} from './home/home.component'
import {AuthenticatedGuard} from './authenticated.guard'
import {AnonymousGuard} from './anonymous.guard'
import {PhotoComponent} from './photo/photo.component'
import {AdminComponent} from './admin/admin.component'
import {AdminGuard} from './admin.guard'
import {UserComponent} from './user/user.component'
import {ChangePasswordComponent} from './change-password/change-password.component'
import {AlbumListComponent} from './album-list/album-list.component'
import {AlbumComponent} from './album/album.component'


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
    path: 'admin/user',
    pathMatch: 'full',
    component: UserComponent,
    canActivate: [AdminGuard]
  },
  {
    path: 'user/password',
    pathMatch: 'full',
    component: ChangePasswordComponent,
    canActivate: [AuthenticatedGuard]
  },
  {
    path: 'album',
    pathMatch: 'full',
    component: AlbumListComponent,
    canActivate: [AuthenticatedGuard]
  },
  {
    path: 'album/:id',
    pathMatch: 'full',
    component: AlbumComponent,
    canActivate: [AuthenticatedGuard]
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
