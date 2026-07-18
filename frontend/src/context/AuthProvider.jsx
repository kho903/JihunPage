import { useEffect, useState } from "react";

import { getCurrentMember, loginMember, logoutMember } from "../api/authApi";
import { AuthContext } from "./AuthContext";

export function AuthProvider({ children }) {
  const [currentMember, setCurrentMember] = useState(null);
  const [isAuthLoading, setIsAuthLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    const restoreAuthentication = async () => {
      try {
        const member = await getCurrentMember();

        if (isMounted) {
          setCurrentMember(member);
        }
      } catch {
        if (isMounted) {
          setCurrentMember(null);
        }
      } finally {
        if (isMounted) {
          setIsAuthLoading(false);
        }
      }
    };

    restoreAuthentication();

    return () => {
      isMounted = false;
    };
  }, []);

  const login = async (loginData) => {
    const member = await loginMember(loginData);

    setCurrentMember(member);

    return member;
  };

  const logout = async () => {
    await logoutMember();

    setCurrentMember(null);
  };

  const value = {
    currentMember,
    isAuthLoading,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
