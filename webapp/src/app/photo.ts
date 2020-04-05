export class Photo {
  constructor(
    public readonly id: string,
    public readonly rawUri: string,
    public readonly compressedUri: string
  ) {
  }
}
