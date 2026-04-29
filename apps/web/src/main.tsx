import { QueryClient, QueryClientProvider, useMutation, useQuery } from '@tanstack/react-query';
import { ClipboardList, LogIn, PackageCheck, RefreshCw, Send } from 'lucide-react';
import React, { useMemo, useState } from 'react';
import ReactDOM from 'react-dom/client';
import { useForm } from 'react-hook-form';
import { createOrder, listOrders, listProducts, login, Product } from './api/manual';
import './styles.css';

const queryClient = new QueryClient();

function App() {
  const [page, setPage] = useState(location.pathname === '/orders' ? 'orders' : location.pathname === '/create-order' ? 'create-order' : 'login');
  const [session, setSession] = useState(() => ({
    token: sessionStorage.getItem('minimonk.token'),
    userId: sessionStorage.getItem('minimonk.userId'),
    username: sessionStorage.getItem('minimonk.username'),
  }));

  function navigate(next: string) {
    history.pushState(null, '', `/${next === 'login' ? 'login' : next}`);
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
            <button onClick={() => navigate('create-order')}>Create order</button>
            <button onClick={() => navigate('orders')}>Orders</button>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">
        {page === 'login' && <LoginPage onLogin={(user) => { setSession(user); navigate('create-order'); }} />}
        {page === 'create-order' && <CreateOrderPage session={session} navigate={navigate} />}
        {page === 'orders' && <OrdersPage />}
      </main>
    </div>
  );
}

function LoginPage({ onLogin }: { onLogin: (session: { token: string; userId: string; username: string }) => void }) {
  const form = useForm({ defaultValues: { username: 'customer', password: 'password' } });
  const mutation = useMutation({
    mutationFn: (values: { username: string; password: string }) => login(values.username, values.password),
    onSuccess: (data) => {
      sessionStorage.setItem('minimonk.token', data.token);
      sessionStorage.setItem('minimonk.userId', data.userId);
      sessionStorage.setItem('minimonk.username', data.username);
      onLogin({ token: data.token, userId: data.userId, username: data.username });
    },
  });

  return (
    <section className="max-w-sm">
      <h1>Login</h1>
      <form className="mt-4 grid gap-3" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
        <input {...form.register('username')} placeholder="Username" />
        <input {...form.register('password')} placeholder="Password" type="password" />
        <button className="primary" type="submit"><LogIn size={18} /> Sign in</button>
        {mutation.error && <p className="error">{mutation.error.message}</p>}
      </form>
    </section>
  );
}

function CreateOrderPage({ session, navigate }: { session: { userId: string | null }; navigate: (next: string) => void }) {
  const [quantities, setQuantities] = useState<Record<string, number>>({});
  const [paymentCardNumber, setPaymentCardNumber] = useState('4242424242424242');
  const productsQuery = useQuery({ queryKey: ['products'], queryFn: listProducts, enabled: Boolean(session.userId) });
  const mutation = useMutation({
    mutationFn: () => {
      const items = (productsQuery.data ?? [])
        .filter((product) => (quantities[product.id] ?? 0) > 0)
        .map((product) => ({ productId: product.id, sku: product.sku, name: product.name, quantity: quantities[product.id], unitPrice: product.price }));
      return createOrder({ customerId: session.userId, paymentCardNumber, items });
    },
    onSuccess: () => navigate('orders'),
  });

  const total = useMemo(() => (productsQuery.data ?? []).reduce((sum, product) => sum + (quantities[product.id] ?? 0) * product.price, 0), [productsQuery.data, quantities]);

  if (!session.userId) return <p>Please log in first.</p>;

  return (
    <section>
      <h1>Create Order</h1>
      <div className="mt-4 overflow-hidden border border-zinc-200 bg-white">
        <table>
          <thead><tr><th>SKU</th><th>Product</th><th>Available</th><th>Price</th><th>Qty</th></tr></thead>
          <tbody>
            {(productsQuery.data ?? []).map((product: Product) => (
              <tr key={product.id}>
                <td>{product.sku}</td>
                <td><strong>{product.name}</strong><span>{product.description}</span></td>
                <td>{product.availableQuantity}</td>
                <td>{money(product.price)}</td>
                <td><input type="number" min="0" max={product.availableQuantity} value={quantities[product.id] ?? 0} onChange={(event) => setQuantities({ ...quantities, [product.id]: Number(event.target.value) })} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="mt-4 flex flex-wrap items-center gap-3">
        <input value={paymentCardNumber} onChange={(event) => setPaymentCardNumber(event.target.value)} />
        <strong>Total {money(total)}</strong>
        <button className="primary" onClick={() => mutation.mutate()} disabled={mutation.isPending || total <= 0}><Send size={18} /> Submit</button>
      </div>
      {mutation.error && <p className="error">{mutation.error.message}</p>}
    </section>
  );
}

function OrdersPage() {
  const ordersQuery = useQuery({ queryKey: ['orders'], queryFn: listOrders, refetchInterval: 2500 });
  return (
    <section>
      <div className="flex items-center justify-between">
        <h1>Orders</h1>
        <button onClick={() => ordersQuery.refetch()}><RefreshCw size={18} /></button>
      </div>
      <div className="mt-4 overflow-hidden border border-zinc-200 bg-white">
        <table>
          <thead><tr><th>Order</th><th>Status</th><th>Items</th><th>Total</th><th>Updated</th></tr></thead>
          <tbody>
            {(ordersQuery.data ?? []).map((order) => (
              <tr key={order.orderId}>
                <td><ClipboardList size={16} /> {order.orderId.slice(0, 8)}</td>
                <td><span className="badge">{order.status}</span></td>
                <td>{order.itemCount}</td>
                <td>{money(order.totalAmount)}</td>
                <td>{new Date(order.updatedAt).toLocaleTimeString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

function money(value: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>,
);
