import {Permission} from './permission'

export class SubjectPermissions {
  constructor(
    public readonly subjectId: string,
    public readonly subjectName: string,
    public readonly permissions: Permission[]
  ) {
  }
}
