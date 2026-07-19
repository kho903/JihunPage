import { useState } from "react";
import { NavLink, useNavigate } from "react-router";

import { useAuth } from "../context/useAuth";

function Header() {
  const navigate = useNavigate();

  const { currentMember, isAuthLoading, logout } = useAuth();

  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [logoutError, setLogoutError] = useState("");

  const getNavLinkClass = ({ isActive }) => {
    return isActive ? "nav-link active fw-bold" : "nav-link";
  };

  const handleLogout = async () => {
    if (isLoggingOut) {
      return;
    }

    setIsLoggingOut(true);
    setLogoutError("");

    try {
      await logout();

      navigate("/", { replace: true });
    } catch (error) {
      if (error.status === null || error.status >= 500) {
        setLogoutError("서버에 연결할 수 없어 로그아웃하지 못했습니다.");
        return;
      }

      setLogoutError(error.message ?? "로그아웃 처리 중 오류가 발생했습니다.");
    } finally {
      setIsLoggingOut(false);
    }
  };

  return (
    <header>
      <nav className="navbar navbar-expand-lg bg-body-tertiary">
        <div className="container">
          <NavLink className="navbar-brand fw-bold" to="/" end>
            JihunPage
          </NavLink>

          <button
            type="button"
            className="navbar-toggler"
            data-bs-toggle="collapse"
            data-bs-target="#mainNavbar"
            aria-controls="mainNavbar"
            aria-expanded="false"
            aria-label="메뉴 열기"
          >
            <span className="navbar-toggler-icon"></span>
          </button>

          <div className="collapse navbar-collapse" id="mainNavbar">
            <ul className="navbar-nav ms-auto align-items-lg-center">
              <li className="nav-item">
                <NavLink className={getNavLinkClass} to="/" end>
                  Home
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink
                  className={getNavLinkClass}
                  to="/members/jihun_01/gallery"
                >
                  Gallery
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink className={getNavLinkClass} to="/guestbook">
                  Guestbook
                </NavLink>
              </li>

              {!isAuthLoading && currentMember && (
                <>
                  <li className="nav-item">
                    <span className="navbar-text px-lg-2 fw-semibold text-primary">
                      {currentMember.userid}님
                    </span>
                  </li>

                  <li className="nav-item">
                    <button
                      type="button"
                      className="nav-link border-0 bg-transparent"
                      onClick={handleLogout}
                      disabled={isLoggingOut}
                    >
                      {isLoggingOut ? "Logging out..." : "Logout"}
                    </button>
                  </li>
                </>
              )}

              {!isAuthLoading && !currentMember && (
                <>
                  <li className="nav-item">
                    <NavLink className={getNavLinkClass} to="/login">
                      Login
                    </NavLink>
                  </li>

                  <li className="nav-item">
                    <NavLink className={getNavLinkClass} to="/signup">
                      Signup
                    </NavLink>
                  </li>
                </>
              )}
            </ul>
          </div>
        </div>
      </nav>

      {logoutError && (
        <div className="container mt-2">
          <div className="alert alert-danger py-2 mb-0" role="alert">
            {logoutError}
          </div>
        </div>
      )}
    </header>
  );
}
export default Header;
