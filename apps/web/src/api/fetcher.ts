export async function apiFetch<T>({ url, method, data, params }: { url: string; method: string; data?: unknown; params?: Record<string, string> }): Promise<T> {
  const search = params ? `?${new URLSearchParams(params).toString()}` : '';
  const token = sessionStorage.getItem('minimonk.token');
  const response = await fetch(`/api${url}${search}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: data ? JSON.stringify(data) : undefined,
  });
  if (!response.ok) {
    throw new Error(await response.text());
  }
  return response.status === 204 ? (undefined as T) : response.json();
}
