import React from "react";
import ReactDOM from "react-dom/client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { createBrowserRouter, RouterProvider } from "react-router-dom";

import App from "./App";
import Catalogue from "./routes/Catalogue";
import BookDetail from "./routes/BookDetail";
import Cart from "./routes/Cart";
import Checkout from "./routes/Checkout";
import OrderConfirmed from "./routes/OrderConfirmed";
import NotFound from "./routes/NotFound";
import "./index.css";

// Every route is registered here up front. Route components own their own
// file and nothing else, so they can be built independently.
const router = createBrowserRouter([
  {
    path: "/",
    element: <App />,
    children: [
      { index: true, element: <Catalogue /> },
      { path: "books/:isbn", element: <BookDetail /> },
      { path: "cart", element: <Cart /> },
      { path: "checkout", element: <Checkout /> },
      { path: "orders/:reference", element: <OrderConfirmed /> },
      { path: "*", element: <NotFound /> },
    ],
  },
]);

const queryClient = new QueryClient({
  defaultOptions: {
    queries: { staleTime: 30_000, retry: 1 },
  },
});

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  </React.StrictMode>,
);
