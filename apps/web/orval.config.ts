import { defineConfig } from 'orval';

export default defineConfig({
  warehouse: {
    input: 'http://localhost:8082/v3/api-docs',
    output: {
      target: './src/api/warehouse.gen.ts',
      client: 'react-query',
      override: {
        mutator: {
          path: './src/api/fetcher.ts',
          name: 'apiFetch',
        },
      },
    },
  },
  orders: {
    input: 'http://localhost:8083/v3/api-docs',
    output: {
      target: './src/api/orders.gen.ts',
      client: 'react-query',
      override: {
        mutator: {
          path: './src/api/fetcher.ts',
          name: 'apiFetch',
        },
      },
    },
  },
});
