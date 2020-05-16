import {Permission} from './permission'

export class SubjectPermissionsUpdate {
  constructor(
    public readonly subjectId: string,
    public readonly permissions: Permission[]
  ) {
  }
}
