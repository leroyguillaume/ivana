import {OwnableEntity} from './ownable-entity'

export class Photo implements OwnableEntity {
  constructor(
    public readonly id: string,
    public readonly ownerId: string,
    public readonly rawUri: string,
    public readonly compressedUri: string
  ) {
  }
}
