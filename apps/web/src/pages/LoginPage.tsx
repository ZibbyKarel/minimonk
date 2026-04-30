import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "@tanstack/react-router";
import { LogIn } from "lucide-react";
import { useForm } from "react-hook-form";
import { login } from "../api/manual";
import { useSession } from "../session";

export function LoginPage() {
  const navigate = useNavigate();
  const { setSession } = useSession();
  const form = useForm({
    defaultValues: { username: "customer", password: "password" },
  });
  const mutation = useMutation({
    mutationFn: (values: { username: string; password: string }) =>
      login(values.username, values.password),
    onSuccess: (data) => {
      const nextSession = {
        token: data.token,
        userId: data.userId,
        username: data.username,
      };

      sessionStorage.setItem("minimonk.token", data.token);
      sessionStorage.setItem("minimonk.userId", data.userId);
      sessionStorage.setItem("minimonk.username", data.username);
      setSession(nextSession);
      void navigate({ to: "/create-order" });
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
