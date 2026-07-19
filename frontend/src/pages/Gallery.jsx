import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router";

import { getPublicGallery } from "../api/galleryApi";
import PhotoModal from "../components/gallery/PhotoModal";
import { useAuth } from "../context/useAuth";

import "./css/Gallery.css";

function Gallery() {
  const { userid } = useParams();
  const { currentMember } = useAuth();
  const location = useLocation();

  const [gallery, setGallery] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPhoto, setSelectedPhoto] = useState(null);

  const refreshGallery = location.state?.refreshGallery;
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
          code: requestError.code,
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
  }, [userid, refreshGallery]);

  const openPhotoModal = (photo) => {
    setSelectedPhoto(photo);
  };

  const closePhotoModal = () => {
    setSelectedPhoto(null);
  };

  if (loading) {
    return (
      <section className="gallery-state">
        <p className="text-secondary mb-0">갤러리를 불러오는 중입니다...</p>
      </section>
    );
  }

  if (error?.code === "GALLERY_OWNER_NOT_FOUND") {
    return (
      <section className="gallery-state">
        <h1 className="mb-3">Gallery</h1>

        <p className="text-secondary mb-0">존재하지 않는 회원입니다.</p>
      </section>
    );
  }

  if (error) {
    return (
      <section className="gallery-state">
        <h1 className="mb-3">Gallery</h1>

        <p className="text-danger mb-0">{error.message}</p>
      </section>
    );
  }

  const { owner, photos } = gallery;
  const isOwner = currentMember?.userid === owner.userid;

  return (
    <section className="gallery-page">
      <header className="gallery-header">
        <h1 className="gallery-title">{owner.username}&apos;s Gallery</h1>

        <p className="gallery-userid">@{owner.userid}</p>

        <p className="gallery-count">사진 {photos.length}장</p>
        {isOwner && (
          <div className="gallery-header-actions">
            <Link to="/gallery/upload" className="btn btn-primary">
              사진 등록
            </Link>
          </div>
        )}
      </header>

      {photos.length === 0 ? (
        <div className="gallery-empty">
          <p className="mb-2">아직 등록된 사진이 없습니다.</p>

          <p className="text-secondary mb-0">첫 번째 사진을 등록해 보세요.</p>
        </div>
      ) : (
        <div className="gallery-grid">
          {photos.map((photo) => (
            <button
              key={photo.id}
              type="button"
              className="gallery-photo-button"
              aria-label={`${photo.title} 사진 크게 보기`}
              onClick={() => openPhotoModal(photo)}
            >
              <img
                className="gallery-photo-image"
                src={photo.imageUrl}
                alt={photo.title || "갤러리 사진"}
              />
            </button>
          ))}
        </div>
      )}
      {selectedPhoto && (
        <PhotoModal photo={selectedPhoto} onClose={closePhotoModal} />
      )}
    </section>
  );
}

export default Gallery;
