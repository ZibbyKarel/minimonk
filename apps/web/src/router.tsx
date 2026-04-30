import {
  createRootRoute,
  createRoute,
  createRouter,
} from "@tanstack/react-router";
import { CreateOrderPage } from "./pages/CreateOrderPage";
import { IndexRedirect } from "./pages/IndexRedirect";
import { LoginPage } from "./pages/LoginPage";
import { OrdersPage } from "./pages/OrdersPage";
import { RootLayout } from "./pages/RootLayout";

const rootRoute = createRootRoute({
  component: RootLayout,
});

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  component: IndexRedirect,
});

const loginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/login",
  component: LoginPage,
});

const createOrderRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/create-order",
  component: CreateOrderPage,
});

const ordersRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/orders",
  component: OrdersPage,
});

const routeTree = rootRoute.addChildren([
  indexRoute,
  loginRoute,
  createOrderRoute,
  ordersRoute,
]);

export const router = createRouter({
  routeTree,
  defaultPreload: "intent",
});

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}
