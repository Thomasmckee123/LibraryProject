import { Link, NavLink, Outlet } from "react-router-dom";

const navLink = ({ isActive }: { isActive: boolean }) =>
  `text-sm font-sans transition-colors hover:text-accent ${
    isActive ? "text-accent" : "text-ink-soft"
  }`;

/** Shell: masthead, nav, and the routed page. */
export default function App() {
  return (
    <div className="mx-auto flex min-h-screen max-w-6xl flex-col px-6">
      <header className="gilt-rule flex items-baseline justify-between gap-6 py-8">
        <Link to="/" className="text-2xl font-semibold tracking-tight text-ink">
          The Bindery
        </Link>
        <nav className="flex gap-6">
          <NavLink to="/" end className={navLink}>
            Catalogue
          </NavLink>
          <NavLink to="/cart" className={navLink}>
            Cart
          </NavLink>
        </nav>
      </header>

      <main className="flex-1 py-10">
        <Outlet />
      </main>

      <footer className="border-t border-rule py-6 font-sans text-xs text-muted">
        <p>A learning project. No real payments are taken.</p>
      </footer>
    </div>
  );
}
