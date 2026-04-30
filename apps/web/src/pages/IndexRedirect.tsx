import { Navigate } from "@tanstack/react-router";
import { useSession } from "../session";

export function IndexRedirect() {
  const { session } = useSession();

  return (
    <Navigate to={session.userId ? "/create-order" : "/login"} replace />
  );
}
