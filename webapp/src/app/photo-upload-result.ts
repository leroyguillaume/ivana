import {Photo} from './photo'

export class PhotoUploadResult {
  constructor(
    public readonly type: string,
    public readonly photo?: Photo,
    public readonly error?: Error
  ) {
  }
}
