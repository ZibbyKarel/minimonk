export async function apiFetch<T>({
  url,
  method,
  data,
  params,
  headers,
  signal,
}: {
  url: string;
  method: string;
  data?: unknown;
  params?: Record<string, string>;
  headers?: Record<string, string>;
  signal?: AbortSignal;
}): Promise<T> {
  const search = params ? `?${new URLSearchParams(params).toString()}` : '';
  const token = sessionStorage.getItem('minimonk.token');
  const response = await fetch(`/api${url}${search}`, {
    method,
    signal,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: data ? JSON.stringify(data) : undefined,
  });
  if (!response.ok) {
    throw new Error(await response.text());
  }
  return response.status === 204 ? (undefined as T) : response.json();
}
