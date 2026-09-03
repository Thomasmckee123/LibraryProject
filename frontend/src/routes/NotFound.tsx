import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <section className="flex flex-col gap-4">
      <h1 className="text-4xl font-semibold">Page not found</h1>
      <p className="font-sans text-sm text-accent">
        <Link to="/">Back to the catalogue</Link>
      </p>
    </section>
  );
}
