export type LoginResponse = {
  token: string;
  userId: string;
  username: string;
  roles: string[];
};

export type Product = {
  id: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  availableQuantity: number;
  reservedQuantity: number;
};

export type OrderOverview = {
  orderId: string;
  customerId: string;
  status: string;
  totalAmount: number;
  itemCount: number;
  createdAt: string;
  updatedAt: string;
};

function authHeaders(): Record<string, string> {
  const token = sessionStorage.getItem('minimonk.token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function login(username: string, password: string) {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) throw new Error('Login failed');
  return response.json() as Promise<LoginResponse>;
}

export async function listProducts() {
  const response = await fetch('/api/products', { headers: authHeaders() });
  if (!response.ok) throw new Error('Products failed');
  return response.json() as Promise<Product[]>;
}

export async function createOrder(payload: unknown) {
  const response = await fetch('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error(await response.text());
  return response.json();
}

export async function listOrders() {
  const response = await fetch('/api/orders', { headers: authHeaders() });
  if (!response.ok) throw new Error('Orders failed');
  return response.json() as Promise<OrderOverview[]>;
}
