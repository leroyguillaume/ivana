import {SubjectPermissionsUpdate} from './subject-permissions-update'

export class PermissionsUpdateEvent {
  constructor(
    public readonly permissionsToAdd: SubjectPermissionsUpdate[],
    public readonly permissionsToRemove: SubjectPermissionsUpdate[],
  ) {
  }
}
