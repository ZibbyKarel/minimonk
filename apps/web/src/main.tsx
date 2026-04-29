import {
  QueryClient,
  QueryClientProvider,
  useMutation,
} from "@tanstack/react-query";
import {
  ClipboardList,
  LogIn,
  PackageCheck,
  RefreshCw,
  Send,
} from "lucide-react";
import React, { useMemo, useState } from "react";
import ReactDOM from "react-dom/client";
import { useForm } from "react-hook-form";
import { login } from "./api/manual";
import { useCreate, useList, type OrderOverviewDto } from "./api/orders.gen";
import { useListProducts, type ProductDto } from "./api/warehouse.gen";
import "./styles.css";

const queryClient = new QueryClient();
const IN_PROGRESS_STATUSES = new Set([
  "CREATED",
  "STOCK_RESERVED",
  "PAYMENT_PENDING",
  "PAYMENT_FAILED",
  "STOCK_RELEASED",
]);

function App() {
  const [page, setPage] = useState(
    location.pathname === "/orders"
      ? "orders"
      : location.pathname === "/create-order"
        ? "create-order"
        : "login",
  );
  const [session, setSession] = useState(() => ({
    token: sessionStorage.getItem("minimonk.token"),
    userId: sessionStorage.getItem("minimonk.userId"),
    username: sessionStorage.getItem("minimonk.username"),
  }));

  function navigate(next: string) {
    history.pushState(null, "", `/${next === "login" ? "login" : next}`);
    setPage(next);
  }

  return (
    <div className="min-h-screen bg-zinc-50 text-zinc-950">
      <header className="border-b border-zinc-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <div className="flex items-center gap-2 font-semibold">
            <PackageCheck size={22} />
            Minimonk Warehouse
          </div>
          <nav className="flex gap-2">
            <button onClick={() => navigate("create-order")}>
              Create order
            </button>
            <button onClick={() => navigate("orders")}>Orders</button>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">
        {page === "login" && (
          <LoginPage
            onLogin={(user) => {
              setSession(user);
              navigate("create-order");
            }}
          />
        )}
        {page === "create-order" && (
          <CreateOrderPage session={session} navigate={navigate} />
        )}
        {page === "orders" && <OrdersPage />}
      </main>
    </div>
  );
}

function LoginPage({
  onLogin,
}: {
  onLogin: (session: {
    token: string;
    userId: string;
    username: string;
  }) => void;
}) {
  const form = useForm({
    defaultValues: { username: "customer", password: "password" },
  });
  const mutation = useMutation({
    mutationFn: (values: { username: string; password: string }) =>
      login(values.username, values.password),
    onSuccess: (data) => {
      sessionStorage.setItem("minimonk.token", data.token);
      sessionStorage.setItem("minimonk.userId", data.userId);
      sessionStorage.setItem("minimonk.username", data.username);
      onLogin({
        token: data.token,
        userId: data.userId,
        username: data.username,
      });
    },
  });

  return (
    <section className="max-w-sm">
      <h1>Login</h1>
      <form
        className="mt-4 grid gap-3"
        onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
      >
        <input {...form.register("username")} placeholder="Username" />
        <input
          {...form.register("password")}
          placeholder="Password"
          type="password"
        />
        <button className="primary" type="submit">
          <LogIn size={18} /> Sign in
        </button>
        {mutation.error && <p className="error">{mutation.error.message}</p>}
      </form>
    </section>
  );
}

function CreateOrderPage({
  session,
  navigate,
}: {
  session: { userId: string | null };
  navigate: (next: string) => void;
}) {
  const [quantities, setQuantities] = useState<Record<string, number>>({});
  const [paymentCardNumber, setPaymentCardNumber] =
    useState("4242424242424242");
  const productsQuery = useListProducts({
    query: {
      enabled: Boolean(session.userId),
    },
  });
  const mutation = useCreate({
    mutation: {
      onSuccess: () => navigate("orders"),
    },
  });

  function submit() {
    const items = (productsQuery.data ?? [])
      .filter((product) => product.id && (quantities[product.id] ?? 0) > 0)
      .map((product) => {
        const id = product.id!;
        return {
          productId: id,
          sku: product.sku ?? "",
          name: product.name ?? "",
          quantity: quantities[id] ?? 0,
          unitPrice: product.price ?? 0,
        };
      });
    mutation.mutate({
      data: {
        customerId: session.userId ?? undefined,
        paymentCardNumber,
        items,
      },
    });
  }

  const total = useMemo(
    () =>
      (productsQuery.data ?? []).reduce(
        (sum, product) =>
          sum +
          (product.id ? quantities[product.id] ?? 0 : 0) *
            (product.price ?? 0),
        0,
      ),
    [productsQuery.data, quantities],
  );

  if (!session.userId) return <p>Please log in first.</p>;

  return (
    <section>
      <h1>Create Order</h1>
      {productsQuery.isPending && <p className="muted">Loading products...</p>}
      {productsQuery.error ? (
        <p className="error">
          Could not load products: {errorMessage(productsQuery.error)}
        </p>
      ) : null}
      <div className="mt-4 overflow-hidden border border-zinc-200 bg-white">
        <table>
          <thead>
            <tr>
              <th>SKU</th>
              <th>Product</th>
              <th>Available</th>
              <th>Price</th>
              <th>Qty</th>
            </tr>
          </thead>
          <tbody>
            {(productsQuery.data ?? []).map((product: ProductDto) => (
              <tr key={product.id}>
                <td>{product.sku}</td>
                <td>
                  <strong>{product.name}</strong>
                  <span>{product.description}</span>
                </td>
                <td>{product.availableQuantity ?? 0}</td>
                <td>{money(product.price ?? 0)}</td>
                <td>
                  <input
                    type="number"
                    min="0"
                    max={product.availableQuantity ?? 0}
                    value={product.id ? quantities[product.id] ?? 0 : 0}
                    onChange={(event) =>
                      product.id &&
                      setQuantities({
                        ...quantities,
                        [product.id]: Number(event.target.value),
                      })
                    }
                  />
                </td>
              </tr>
            ))}
            {productsQuery.data?.length === 0 && (
              <tr>
                <td colSpan={5}>No products are available.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      <div className="mt-4 flex flex-wrap items-center gap-3">
        <input
          value={paymentCardNumber}
          onChange={(event) => setPaymentCardNumber(event.target.value)}
        />
        <strong>Total {money(total)}</strong>
        <button
          className="primary"
          onClick={submit}
          disabled={mutation.isPending || total <= 0}
        >
          <Send size={18} /> Submit
        </button>
      </div>
      {mutation.error ? (
        <p className="error">{errorMessage(mutation.error)}</p>
      ) : null}
    </section>
  );
}

function OrdersPage() {
  const ordersQuery = useList({
    query: {
      refetchInterval: (query) =>
        query.state.data?.some((order: OrderOverviewDto) =>
          order.status ? IN_PROGRESS_STATUSES.has(order.status) : false,
        )
          ? 2500
          : false,
    },
  });
  return (
    <section>
      <div className="flex items-center justify-between">
        <h1>Orders</h1>
        <button onClick={() => ordersQuery.refetch()}>
          <RefreshCw size={18} />
        </button>
      </div>
      {ordersQuery.isPending && <p className="muted">Loading orders...</p>}
      {ordersQuery.error ? (
        <p className="error">
          Could not load orders: {errorMessage(ordersQuery.error)}
        </p>
      ) : null}
      <div className="mt-4 overflow-hidden border border-zinc-200 bg-white">
        <table>
          <thead>
            <tr>
              <th>Order</th>
              <th>Status</th>
              <th>Items</th>
              <th>Total</th>
              <th>Updated</th>
            </tr>
          </thead>
          <tbody>
            {(ordersQuery.data ?? []).map((order) => (
              <tr key={order.orderId}>
                <td>
                  <ClipboardList size={16} /> {order.orderId?.slice(0, 8)}
                </td>
                <td>
                  <span className="badge">{order.status}</span>
                </td>
                <td>{order.itemCount ?? 0}</td>
                <td>{money(order.totalAmount ?? 0)}</td>
                <td>
                  {order.updatedAt
                    ? new Date(order.updatedAt).toLocaleTimeString()
                    : ""}
                </td>
              </tr>
            ))}
            {ordersQuery.data?.length === 0 && (
              <tr>
                <td colSpan={5}>No orders yet.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function money(value: number) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(value);
}

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Request failed";
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>,
);
