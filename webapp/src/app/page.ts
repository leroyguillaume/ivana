export class Page<E> {
  constructor(
    public readonly content: E[],
    public readonly no: number,
    public readonly totalItems: number,
    public readonly totalPages: number
  ) {
  }
}
