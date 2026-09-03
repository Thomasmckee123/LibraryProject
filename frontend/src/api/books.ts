import { api } from "./client";
import type { Book } from "../types";

/**
 * All /api/books calls live here - see frontend/CLAUDE.md.
 */

export function listBooks(params?: { author?: string; q?: string }): Promise<Book[]> {
  const query = new URLSearchParams();
  if (params?.author) query.set("author", params.author);
  if (params?.q) query.set("q", params.q);
  const qs = query.toString();
  return api.get<Book[]>(`/books${qs ? `?${qs}` : ""}`);
}

export function getBook(isbn: string): Promise<Book> {
  return api.get<Book>(`/books/${encodeURIComponent(isbn)}`);
}
