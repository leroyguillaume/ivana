import {Photo} from './photo'
import {OwnableEntity} from './ownable-entity'
import {Permission} from './permission'

export class NavigablePhoto implements OwnableEntity {
  constructor(
    public readonly id: string,
    public readonly ownerId: string,
    public readonly rawUri: string,
    public readonly compressedUri: string,
    public readonly shootingDate: Date,
    public readonly permissions: Permission[],
    public readonly previous: Photo,
    public readonly next: Photo
  ) {
  }
}
