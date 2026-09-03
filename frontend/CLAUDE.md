# frontend

React storefront for the bookshop. Vite, TypeScript, React Router.

**Nothing here yet.** This file is the plan, not a description.

## Stack

| Piece | Choice | Why |
|---|---|---|
| Build | Vite | Fast, and the current default for a new React app |
| Language | TypeScript | Catches the shape mismatches that make API work painful |
| Routing | React Router | Standard for a multi-page SPA |
| Data | TanStack Query | Handles caching, loading, and refetch so components don't |
| Styling | Plain CSS Modules | No framework to learn on top of React itself |

No component library. Buttons and cards are worth writing once by hand when
you're learning what they're made of.

## Layout

```
src/
  main.tsx
  routes/
    Catalogue.tsx     browse and search
    BookDetail.tsx    one book, add to cart
    Cart.tsx          lines, quantities, total
    Checkout.tsx      confirm, place order
    OrderConfirmed.tsx
  components/         Button, BookCard, QuantityInput, Price
  api/                client.ts, books.ts, cart.ts, orders.ts — all fetch calls
  types.ts            mirrors the backend DTOs
```

Every `fetch` lives in `api/`. A component that calls `fetch` directly is
untestable without a network, and the URL ends up duplicated across files.

## Conventions

**Server state is not component state.** Books, carts, and orders live on
the server — read them with TanStack Query, don't copy them into
`useState`. A local copy drifts the moment anything else changes it.
`useState` is for genuinely local things: which tab is open, what's typed
in a search box.

**Money is formatted, never computed.** The backend sends the total. The
frontend renders it with `Intl.NumberFormat`. Two implementations of the
same arithmetic will eventually disagree, and the one showing the customer a
different number is the one that matters.

**Every fetch has three states.** Loading, error, empty. A page that only
handles the happy path shows a blank screen the first time the API is slow
or down.

**Types mirror the API.** `types.ts` matches the backend DTOs exactly. When
an endpoint changes, that file changes first.

## Prices and stock

Show stock honestly — "In stock", "Only 2 left", "Out of stock" — and
disable the add-to-cart control when it's zero. The backend still rejects
the order, but a button that fails when pressed is a bad experience.

Checkout can fail with a 409 when someone else buys the last copy first.
That is a normal outcome, not a crash: say which book is unavailable and
let them adjust the cart.

## Running it

Vite dev server on 5173, API on 8080. Proxy `/api` in `vite.config.ts`
rather than hardcoding `localhost:8080` — that way the same code works
when both are served from one origin in production.
