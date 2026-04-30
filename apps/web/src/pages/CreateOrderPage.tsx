import { useNavigate } from "@tanstack/react-router";
import { Send } from "lucide-react";
import React, { useMemo, useState } from "react";
import { useCreate } from "../api/orders.gen";
import { useListProducts, type ProductDto } from "../api/warehouse.gen";
import { useSession } from "../session";
import { errorMessage, money } from "./utils";

export function CreateOrderPage() {
  const navigate = useNavigate();
  const { session } = useSession();
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
      onSuccess: () => {
        void navigate({ to: "/orders" });
      },
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

  if (!session.userId) {
    return <p>Please log in first.</p>;
  }

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
