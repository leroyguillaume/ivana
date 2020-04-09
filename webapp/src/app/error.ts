export class Error {
  constructor(
    public readonly code: string,
    public readonly resourceUri?: string
  ) {
  }
}
