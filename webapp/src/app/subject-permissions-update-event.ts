import {SubjectPermissionsUpdate} from './subject-permissions-update'

export class SubjectPermissionsUpdateEvent {
  constructor(
    public readonly subjsPermsToAdd: SubjectPermissionsUpdate[],
    public readonly subjsPermsToRemove: SubjectPermissionsUpdate[]
  ) {
  }
}
