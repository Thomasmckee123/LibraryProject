import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <section className="placeholder">
      <h1>Page not found</h1>
      <p>
        <Link to="/">Back to the catalogue</Link>
      </p>
    </section>
  );
}
