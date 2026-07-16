import { NavLink } from "react-router";

function Header() {
  const getNavLinkClass = ({ isActive }) => {
    return isActive ? "nav-link active fw-bold" : "nav-link";
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
            arias-controls="mainNavbar"
            arias-expanded="false"
            arias-label="메뉴 열기"
          >
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="collapse navbar-collapse" id="mainNavbar">
            <ul className="navbar-nav ms-auto">
              <li className="nav-item">
                <NavLink className={getNavLinkClass} to="/" end>
                  Home
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink className={getNavLinkClass} to="/gallery">
                  Gallery
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink className={getNavLinkClass} to="/guestbook">
                  Guestbook
                </NavLink>
              </li>
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
            </ul>
          </div>
        </div>
      </nav>
    </header>
  );
}
export default Header;
