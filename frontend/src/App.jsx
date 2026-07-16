import { Route, Routes } from "react-router";

import Gallery from "./pages/Gallery";
import Guestbook from "./pages/Guestbook";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Signup from "./pages/Signup";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />}></Route>
      <Route path="/signup" element={<Signup />}></Route>
      <Route path="/login" element={<Login />}></Route>
      <Route path="/guestbook" element={<Guestbook />}></Route>
      <Route path="/gallery" element={<Gallery />}></Route>
    </Routes>
  );
}

export default App;
