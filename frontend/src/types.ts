// Mirrors the backend DTOs exactly. When an endpoint changes, this changes first.

export interface Book {
  isbn: string;
  title: string;
  author: string;
  price: string; // BigDecimal on the wire - a string, never a JS number
  stock: number;
}

export interface CartLine {
  isbn: string;
  title: string;
  author: string;
  unitPrice: string;
  quantity: number;
  lineTotal: string;
}

export interface Cart {
  id: number;
  lines: CartLine[];
  total: string;
}

export interface OrderLine {
  isbn: string;
  titleAtPurchase: string;
  unitPrice: string;
  quantity: number;
  lineTotal: string;
}

export interface Order {
  id: number;
  reference: string;
  placedAt: string;
  status: "PAID" | "CANCELLED";
  lines: OrderLine[];
  total: string;
}

export interface ApiError {
  status: number;
  message: string;
  isbn?: string;
  requested?: number;
  available?: number;
}
