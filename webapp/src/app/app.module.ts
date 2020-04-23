import {BrowserModule} from '@angular/platform-browser'
import {NgModule} from '@angular/core'

import {AppRoutingModule} from './app-routing.module'
import {AppComponent} from './app.component'
import {LoginComponent} from './login/login.component'
import {ReactiveFormsModule} from '@angular/forms'
import {HttpClientModule} from '@angular/common/http'
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome'
import {NgbModule} from '@ng-bootstrap/ng-bootstrap'
import {HomeComponent} from './home/home.component'
import {NavbarComponent} from './navbar/navbar.component'
import {PhotoComponent} from './photo/photo.component'
import {AdminComponent} from './admin/admin.component'
import {UserComponent} from './user/user.component'
import {ErrorComponent} from './error/error.component'
import {ChangePasswordComponent} from './change-password/change-password.component'
import {PhotoGridComponent} from './photo-grid/photo-grid.component'
import {AlbumListComponent} from './album-list/album-list.component'
import {AlbumModalComponent} from './album-modal/album-modal.component'
import {AlbumGridComponent} from './album-grid/album-grid.component'

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    HomeComponent,
    NavbarComponent,
    PhotoComponent,
    AdminComponent,
    UserComponent,
    ErrorComponent,
    ChangePasswordComponent,
    PhotoGridComponent,
    AlbumListComponent,
    AlbumModalComponent,
    AlbumGridComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    FontAwesomeModule,
    NgbModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
