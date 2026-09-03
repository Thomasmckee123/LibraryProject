# frontend

React storefront for the bookshop. Vite, TypeScript, React Router, TanStack
Query, **Tailwind CSS v4**.

## Stack

| Piece | Choice | Why |
|---|---|---|
| Build | Vite 8 | Fast; the current default for React |
| Language | TypeScript (strict) | Catches the shape mismatches that make API work painful |
| Routing | React Router 7 | Standard for a multi-page SPA |
| Data | TanStack Query 5 | Caching, loading, refetch — so components don't |
| Styling | Tailwind CSS v4 | Utilities in the markup; one design system, no CSS files to drift |

No component library. Buttons and cards are worth writing once by hand.

## Tailwind v4 — this is not v3

**There is no `tailwind.config.js`.** v4 is configured in CSS. The theme lives
in an `@theme` block in `src/index.css`, and the plugin is wired in
`vite.config.ts` via `@tailwindcss/vite`. If you find yourself reaching for a
config file or `npx tailwindcss init`, you are following v3 instructions.

```css
@import "tailwindcss";

@theme {
  --color-paper: #efede4;   /* light palette lives here */
  --color-accent: #2a4b7c;
}

@media (prefers-color-scheme: dark) {
  :root {                    /* dark overrides plain custom properties */
    --color-paper: #14171c;
    --color-accent: #86aade;
  }
}
```

Every token in `@theme` becomes a utility automatically — `--color-paper` gives
you `text-paper`, `bg-paper`, `border-paper`. Do not hand-write a utility for a
value that should be a token.

**Never put `@theme` inside a media query.** It is processed at build time and
is not conditional, so it overwrites the base values unconditionally rather
than applying only in that media context. This shipped once and made the light
theme unreachable — the whole site rendered dark regardless of OS setting.
Declare the light palette in `@theme`, then override the plain custom
properties on `:root` inside the media query, as above. Utilities resolve
through `var()`, so they follow automatically.

Verify theme changes by rendering both, not by reading the CSS:

```js
// colorScheme: "light" | "dark" in a Playwright newPage()
const bg = await page.evaluate(() => getComputedStyle(document.body).backgroundColor);
```

## Design tokens

Defined once in `src/index.css`. **Never hardcode a colour in a component.**

The palette is taken from bookbinding: marbled endpaper indigo as the brand,
gilt foil stamping, oxblood for error, and a laid-paper ground rather than
cream. Dark is a cloth board under lamplight — blue-black, never neutral black.

| Token | Use |
|---|---|
| `paper` / `surface` / `sunk` | Page ground, card ground, recessed wells |
| `ink` / `ink-soft` / `muted` | Primary text, secondary text, captions |
| `rule` / `rule-strong` | Borders and dividers |
| `accent` / `accent-soft` | Marbled indigo — links, primary buttons |
| `gilt` | Foil stamping. Decorative edges and hover states only |
| `good` / `warn` / `bad` | In stock / low stock / out of stock and errors |

Semantic state colours are separate from `accent` and must stay that way — a
stock warning that renders in the brand colour tells the reader nothing.

Dark mode is handled at token level in one `@media (prefers-color-scheme: dark)`
block. A component that writes `dark:` variants everywhere has bypassed the
system; fix the token instead.

## Layout

```
src/
  main.tsx            router + query client, all routes registered here
  App.tsx             shell: masthead, nav, footer
  index.css           @theme tokens, base layer, the few real component classes
  routes/             one file per page
  components/         reusable pieces
  api/                every fetch call — client, books, cart, orders, money
  types.ts            mirrors backend DTOs exactly
```

## Conventions

**Every fetch lives in `api/`.** A component calling `fetch` directly is
untestable without a network and duplicates the URL.

**Server state is not component state.** Books, carts, and orders live on the
server — read them with TanStack Query, never copy into `useState`. `useState`
is for genuinely local things: a search box's text, whether a panel is open.

**Money is formatted, never computed.** The backend sends every total; render it
with `formatPrice`. `price` is a **string** on the wire deliberately — it is a
`BigDecimal` server-side. Never do arithmetic on it. Two implementations of the
same sum eventually disagree, and the one the customer sees is the one that
matters.

**Every query handles three states**: loading, error, empty. A page that only
handles the happy path shows a blank screen the first time the API is slow.

**Types mirror the API.** `types.ts` matches the backend DTOs exactly. JSON is
cast, not validated, so a field-name mismatch is invisible to `tsc` and shows up
as `undefined` in the UI — this has already happened twice on this project
(`items` vs `lines`, `title` vs `titleAtPurchase`). When an endpoint changes,
change `types.ts` first, then run the e2e suite.

**Repeated utility strings become a component, not an `@apply`.** If three
places need the same button, write `<Button>`. `@apply` recreates the CSS-file
problem Tailwind exists to solve.

## Accessibility is not optional

Real `<button>` and `<a>` elements, never a clickable `<div>`. Every icon-only
control needs an `aria-label`. Status messages use `role="status"`, errors
`role="alert"` — the e2e suite selects on these, so breaking them breaks the
build. Keep a visible focus ring; Tailwind's `focus-visible:` variants are
already set up in the base layer.

## Growing this

The shop is small now and the structure is deliberately flat. Before it gets
big:

- **Extract a `Button` and `Field` primitive** the moment a third variant
  appears. Do it before, and you will guess the API wrong.
- **`api/` stays one file per resource.** When a resource grows past a handful
  of calls, split by resource, never by verb.
- **Add a route-level layout** when pages start sharing chrome beyond the
  masthead — React Router nested routes, not a copied wrapper.
- **Cart id is hardcoded to 1** in three places (`Cart.tsx`, `Checkout.tsx`,
  `BookDetail.tsx`). This is the single biggest piece of debt in the frontend.
  The moment accounts exist, that becomes a session lookup — until then, do not
  spread the constant any further.

## Running it

```bash
npm run dev          # :5173, proxies /api to :8080 — start the backend too
npm run typecheck
npm run e2e          # Playwright; starts both servers itself
npm run e2e:ui       # watch it drive the browser
```

There are currently **no component unit tests** — only typecheck and 8 e2e
specs. That is a known gap: e2e is slow and only covers happy paths, so
threshold logic (`StockBadge` boundaries, `QuantityStepper` flooring at 1) and
error branches are unproven. Vitest + React Testing Library is the right fix.
