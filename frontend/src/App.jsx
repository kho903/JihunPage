import { Route, Routes } from "react-router";

import Header from "./components/Header";
import MyGalleryRedirect from "./components/gallery/MyGalleryRedirect";
import Gallery from "./pages/Gallery";
import GalleryUpload from "./pages/GalleryUpload";
import Guestbook from "./pages/Guestbook";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Signup from "./pages/Signup";

function App() {
  return (
    <>
      <Header />
      <main className="container py-4">
        <Routes>
          <Route path="/" element={<Home />}></Route>
          <Route path="/signup" element={<Signup />}></Route>
          <Route path="/login" element={<Login />}></Route>
          <Route path="/guestbook" element={<Guestbook />}></Route>
          <Route path="/gallery" element={<MyGalleryRedirect />} />
          <Route path="/members/:userid/gallery" element={<Gallery />} />
          <Route path="/gallery/upload" element={<GalleryUpload />} />
        </Routes>
      </main>
    </>
  );
}

export default App;
