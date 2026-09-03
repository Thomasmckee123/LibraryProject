import { expect, test } from "@playwright/test";

/**
 * Drives the real UI against a real backend. These are the checks that unit
 * tests structurally cannot make: they cross the frontend/backend seam, which
 * is where every bug in this project has actually lived.
 *
 * Tests run serially and share one cart and one stock pool, so each starts by
 * emptying the cart rather than assuming it is clean.
 */

const DUNE = "9780441013593";

async function emptyTheCart(request: any) {
  const cart = await request.get("/api/carts/1").then((r: any) => r.json());
  for (const line of cart.lines ?? []) {
    await request.delete(`/api/carts/1/items/${line.isbn}`);
  }
}

test.beforeEach(async ({ request }) => {
  await emptyTheCart(request);
});

test("catalogue lists the seeded books", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("link", { name: "The Bindery" })).toBeVisible();
  await expect(page.getByRole("heading", { name: "Dune", exact: true })).toBeVisible();
  await expect(page.getByText("Nineteen Eighty-Four")).toBeVisible();
});

test("search narrows the catalogue by author", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("searchbox").fill("orwell");

  await expect(page.getByText("Nineteen Eighty-Four")).toBeVisible();
  await expect(page.getByText("Emma")).toBeHidden();
});

test("an out-of-stock book cannot be added", async ({ page }) => {
  await page.goto("/books/9780452284241"); // Animal Farm, seeded at zero
  await expect(page.getByText("Out of stock")).toBeVisible();
  await expect(page.getByRole("button", { name: "Add to cart" })).toBeDisabled();
});

test("browse to a book and add it to the cart", async ({ page }) => {
  await page.goto(`/books/${DUNE}`);
  await expect(page.getByRole("heading", { name: "Dune" })).toBeVisible();
  await expect(page.getByText("£9.99")).toBeVisible();

  await page.getByRole("button", { name: "Add to cart" }).click();
  await expect(page.getByRole("status")).toContainText("Added to your cart");

  await page.getByRole("link", { name: "View cart" }).click();
  await expect(page).toHaveURL(/\/cart$/);
  await expect(page.getByText("Dune")).toBeVisible();
});

test("quantity can be decreased as well as increased", async ({ page, request }) => {
  await request.post("/api/carts/1/items", {
    data: { isbn: DUNE, quantity: 1 },
  });

  await page.goto("/cart");
  await expect(page.getByText("£9.99").first()).toBeVisible();

  // Increase to 3.
  const plus = page.getByRole("button", { name: /increase/i });
  await plus.click();
  await expect(page.getByText("£19.98").first()).toBeVisible();
  await plus.click();
  await expect(page.getByText("£29.97").first()).toBeVisible();

  // Decrease back to 2. This is the path that used to 400.
  await page.getByRole("button", { name: /decrease/i }).click();
  await expect(page.getByText("£19.98").first()).toBeVisible();
});

test("an empty cart says so rather than showing an empty table", async ({ page }) => {
  await page.goto("/cart");
  await expect(page.getByText(/empty/i)).toBeVisible();
});

test("checkout produces an order with the price actually charged", async ({
  page,
  request,
}) => {
  const before = await request
    .get(`/api/books/${DUNE}`)
    .then((r) => r.json());

  await request.post("/api/carts/1/items", {
    data: { isbn: DUNE, quantity: 2 },
  });

  await page.goto("/cart");
  await page.getByRole("link", { name: /checkout/i }).click();
  await expect(page).toHaveURL(/\/checkout$/);
  await expect(page.getByText(/no real payment/i)).toBeVisible();

  await page.getByRole("button", { name: /place order/i }).click();

  await expect(page).toHaveURL(/\/orders\/\d+$/);
  await expect(page.getByText(/BND-/)).toBeVisible();
  // 19.98 appears twice - once as the line total, once as the order total.
  await expect(page.getByText("Dune").first()).toBeVisible();
  await expect(page.getByText("£19.98")).toHaveCount(2);

  // Stock actually moved, and the cart was emptied.
  const after = await request.get(`/api/books/${DUNE}`).then((r) => r.json());
  expect(after.stock).toBe(before.stock - 2);

  const cart = await request.get("/api/carts/1").then((r) => r.json());
  expect(cart.lines).toHaveLength(0);
});

test("checkout refuses an order larger than stock and explains why", async ({
  page,
  request,
}) => {
  // Lord of the Rings is seeded at 3 copies.
  await request.post("/api/carts/1/items", {
    data: { isbn: "9780618640157", quantity: 5 },
  });

  await page.goto("/checkout");
  await page.getByRole("button", { name: /place order/i }).click();

  const alert = page.getByRole("alert");
  await expect(alert).toBeVisible();
  await expect(alert).toContainText("3");
  await expect(page).not.toHaveURL(/\/orders\//);
});

test("a placed order appears in the order history and opens from there", async ({
  page,
  request,
}) => {
  await request.post("/api/carts/1/items", {
    data: { isbn: DUNE, quantity: 1 },
  });

  await page.goto("/checkout");
  await page.getByRole("button", { name: /place order/i }).click();
  await expect(page).toHaveURL(/\/orders\/\d+$/);

  // Capture the reference the confirmation screen showed, then navigate away
  // entirely - the point of the history page is finding it again afterwards.
  const reference = await page.getByText(/BND-/).innerText();

  await page.goto("/");
  await page.getByRole("link", { name: "Orders" }).click();
  await expect(page).toHaveURL(/\/orders$/);

  const row = page.getByRole("link", { name: new RegExp(reference) });
  await expect(row).toBeVisible();
  await expect(row).toContainText("£9.99");
  await expect(row).toContainText("1 book");

  // The row links back to the order it summarises.
  await row.click();
  await expect(page).toHaveURL(/\/orders\/\d+$/);
  await expect(page.getByText(reference)).toBeVisible();
  await expect(page.getByText("Dune").first()).toBeVisible();
});

test("order history says so when there is nothing to show", async ({ page }) => {
  // The seeded demo customer is id 1; id 2 has never ordered, so this asks the
  // API directly rather than going through the page's hardcoded customer.
  const empty = await page.request.get("/api/orders?customerId=2");
  expect(empty.ok()).toBeTruthy();
  expect(await empty.json()).toEqual([]);
});

