import { useEffect, useState } from "react";
import { useParams } from "react-router";

import { getPublicGallery } from "../api/galleryApi";

function Gallery() {
  const { userid } = useParams();

  const [gallery, setGallery] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const abortController = new AbortController();

    const fetchGallery = async () => {
      setLoading(true);
      setError(null);
      setGallery(null);

      try {
        const galleryData = await getPublicGallery(
          userid,
          abortController.signal,
        );

        setGallery(galleryData);
      } catch (requestError) {
        if (requestError.name === "AbortError") {
          return;
        }

        setError({
          status: requestError.status,
          message: requestError.message,
        });
      } finally {
        if (!abortController.signal.aborted) {
          setLoading(false);
        }
      }
    };

    fetchGallery();

    return () => {
      abortController.abort();
    };
  }, [userid]);

  if (loading) {
    return (
      <main className="container py-5 text-center">
        <p className="text-secondary mb-0">갤러리를 불러오는 중입니다...</p>
      </main>
    );
  }

  if (error?.status === 404) {
    return (
      <main className="container py-5 text-center">
        <h1 className="mb-3">Gallery</h1>

        <p className="text-secondary mb-0">존재하지 않는 회원입니다.</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="container py-5 text-center">
        <h1 className="mb-3">Gallery</h1>

        <p className="text-danger mb-0">{error.message}</p>
      </main>
    );
  }

  return (
    <main className="container py-5">
      <section className="text-center">
        <h1 className="mb-2">{gallery.owner.username}&apos;s Gallery</h1>

        <p className="text-secondary mb-2">@{gallery.owner.userid}</p>

        <p className="mb-0">사진 {gallery.photos.length}장</p>
      </section>
    </main>
  );
}

export default Gallery;
