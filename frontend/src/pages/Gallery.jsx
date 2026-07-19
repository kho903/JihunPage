import { useParams } from "react-router";

function Gallery() {
  const { userid } = useParams();

  return (
    <main className="container py-5">
      <section className="text-center">
        <h1 className="mb-2">Gallery</h1>

        <p className="text-secondary mb-0">@{userid}</p>
      </section>
    </main>
  );
}

export default Gallery;
