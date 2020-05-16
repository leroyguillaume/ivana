import {Photo} from './photo'
import {OwnableEntity} from './ownable-entity'

export class NavigablePhoto implements OwnableEntity {
  constructor(
    public readonly id: string,
    public readonly ownerId: string,
    public readonly rawUri: string,
    public readonly compressedUri: string,
    public readonly previous: Photo,
    public readonly next: Photo
  ) {
  }
}
