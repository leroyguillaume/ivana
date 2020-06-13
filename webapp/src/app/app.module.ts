import {BrowserModule, HammerModule} from '@angular/platform-browser'
import {LOCALE_ID, NgModule} from '@angular/core'
import fr from '@angular/common/locales/fr'

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
import {AlbumComponent} from './album/album.component'
import {AlbumSelectionModalComponent} from './album-selection-modal/album-selection-modal.component'
import {ForbiddenComponent} from './forbidden/forbidden.component'
import {PhotoUpdateComponent} from './photo-update/photo-update.component'
import {PermissionComponent} from './permission/permission.component'
import {NewPermissionsModalComponent} from './new-permissions-modal/new-permissions-modal.component'
import {PermissionsTableComponent} from './permissions-table/permissions-table.component'
import {AlbumUpdateComponent} from './album-update/album-update.component'
import {AlbumUpdateFormComponent} from './album-update-form/album-update-form.component'
import {PhotoUpdateFormComponent} from './photo-update-form/photo-update-form.component'
import {registerLocaleData} from '@angular/common'

registerLocaleData(fr)

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
    AlbumGridComponent,
    AlbumComponent,
    AlbumSelectionModalComponent,
    ForbiddenComponent,
    PhotoUpdateComponent,
    PermissionComponent,
    NewPermissionsModalComponent,
    PermissionsTableComponent,
    AlbumUpdateComponent,
    AlbumUpdateFormComponent,
    PhotoUpdateFormComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    HttpClientModule,
    FontAwesomeModule,
    NgbModule,
    HammerModule
  ],
  providers: [
    {
      provide: LOCALE_ID,
      useValue: 'fr-FR'
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
