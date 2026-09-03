import type { Cart } from "../types";
import { api } from "./client";

/**
 * Every cart fetch lives here - see frontend/CLAUDE.md. Components call
 * these through TanStack Query rather than reaching for `api` directly.
 */

export function getCart(cartId: number): Promise<Cart> {
  return api.get<Cart>(`/carts/${cartId}`);
}

export function addItem(cartId: number, isbn: string, quantity: number): Promise<Cart> {
  return api.post<Cart>(`/carts/${cartId}/items`, { isbn, quantity });
}

export function removeItem(cartId: number, isbn: string): Promise<Cart> {
  return api.delete<Cart>(`/carts/${cartId}/items/${isbn}`);
}
