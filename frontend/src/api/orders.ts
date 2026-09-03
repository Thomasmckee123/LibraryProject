import type { Order } from "../types";
import { api } from "./client";

/**
 * Every order-related fetch lives here - see frontend/CLAUDE.md.
 */

/** Turns a cart into a paid order. 409s if a line can no longer be filled. */
export function checkout(cartId: number): Promise<Order> {
  return api.post<Order>(`/carts/${cartId}/checkout`);
}

export function getOrder(id: number): Promise<Order> {
  return api.get<Order>(`/orders/${id}`);
}
