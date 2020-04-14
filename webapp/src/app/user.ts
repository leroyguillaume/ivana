import {Role} from './role'

export class User {
  constructor(
    public readonly id: string,
    public readonly name: string,
    public readonly role: Role,
    public readonly creationDate: Date,
  ) {
  }
}

export function isAdmin(user: User): boolean {
  return user && (user.role === Role.Admin || user.role === Role.SuperAdmin)
}
