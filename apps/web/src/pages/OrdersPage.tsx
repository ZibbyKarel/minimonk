import { ClipboardList, RefreshCw } from "lucide-react";
import { useList, type OrderOverviewDto } from "../api/orders.gen";
import { errorMessage, IN_PROGRESS_STATUSES, money } from "./utils";

export function OrdersPage() {
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
        <button onClick={() => void ordersQuery.refetch()}>
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
