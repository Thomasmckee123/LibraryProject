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

/**
 * Sets a line to an absolute quantity.
 *
 * addItem only adds, so a stepper moving downwards has no positive delta to
 * send - the backend rejects a negative quantity with 400. This is the
 * endpoint a stepper should use in both directions.
 */
export function setQuantity(cartId: number, isbn: string, quantity: number): Promise<Cart> {
  return api.put<Cart>(`/carts/${cartId}/items/${isbn}`, { quantity });
}
