import { Link, Outlet } from "react-router-dom";

/** Shell: masthead, nav, and the routed page. Owned by the foundation. */
export default function App() {
  return (
    <div className="shell">
      <header className="masthead">
        <Link to="/" className="wordmark">
          The Bindery
        </Link>
        <nav>
          <Link to="/">Catalogue</Link>
          <Link to="/cart">Cart</Link>
        </nav>
      </header>
      <main>
        <Outlet />
      </main>
      <footer>
        <p>A learning project. No real payments are taken.</p>
      </footer>
    </div>
  );
}
