export enum Role {
  User = 'user',
  Admin = 'admin',
  SuperAdmin = 'super_admin'
}

export const RoleLabels: { role: Role, label: string }[] = [
  {
    role: Role.User,
    label: 'Simple utilisateur'
  },
  {
    role: Role.Admin,
    label: 'Administrateur'
  },
  {
    role: Role.SuperAdmin,
    label: 'Super administrateur'
  }
]
