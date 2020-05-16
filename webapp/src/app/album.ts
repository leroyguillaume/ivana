import {OwnableEntity} from './ownable-entity'

export class Album implements OwnableEntity {
  constructor(
    public readonly id: string,
    public readonly ownerId: string,
    public readonly name: string,
    public readonly creationDate: Date
  ) {
  }
}
