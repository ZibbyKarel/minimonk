import React, { createContext, useContext, useState } from "react";

export type Session = {
  token: string | null;
  userId: string | null;
  username: string | null;
};

type SessionContextValue = {
  session: Session;
  setSession: React.Dispatch<React.SetStateAction<Session>>;
};

const SessionContext = createContext<SessionContextValue | null>(null);

function readStoredSession(): Session {
  return {
    token: sessionStorage.getItem("minimonk.token"),
    userId: sessionStorage.getItem("minimonk.userId"),
    username: sessionStorage.getItem("minimonk.username"),
  };
}

export function SessionProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const [session, setSession] = useState<Session>(() => readStoredSession());

  return (
    <SessionContext.Provider value={{ session, setSession }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  const context = useContext(SessionContext);

  if (!context) {
    throw new Error("useSession must be used within SessionProvider");
  }

  return context;
}
