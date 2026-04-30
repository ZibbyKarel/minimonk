import { Link, Outlet } from "@tanstack/react-router";
import { PackageCheck } from "lucide-react";

export function RootLayout() {
  return (
    <div className="min-h-screen bg-zinc-50 text-zinc-950">
      <header className="border-b border-zinc-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <div className="flex items-center gap-2 font-semibold">
            <PackageCheck size={22} />
            Minimonk Warehouse
          </div>
          <nav className="flex gap-2">
            <Link className="nav-link" to="/create-order">
              Create order
            </Link>
            <Link className="nav-link" to="/orders">
              Orders
            </Link>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
