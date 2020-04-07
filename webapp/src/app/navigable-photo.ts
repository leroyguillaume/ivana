import {Photo} from './photo'

export class NavigablePhoto {
  constructor(
    public readonly id: string,
    public readonly rawUri: string,
    public readonly compressedUri: string,
    public readonly previous?: Photo,
    public readonly next?: Photo
  ) {
  }
}
