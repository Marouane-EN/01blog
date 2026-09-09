import { HttpParams } from '@angular/common/http';

export function buildCursorParams(cursor?: number | null, params = new HttpParams()): HttpParams {
  if (cursor) {
    return params.set('cursor', cursor.toString());
  }
  return params;
}
