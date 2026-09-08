// ── Generic Wrapper ──────────────────────────────────────────────────
export interface CursorResponse<T> {
  readonly data: readonly T[];
  readonly nextCursor: number | null;
  readonly hasMore: boolean;
}
 