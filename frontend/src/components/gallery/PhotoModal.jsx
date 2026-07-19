import { useEffect } from "react";

import "../css/PhotoModal.css";

function PhotoModal({ photo, onClose }) {
  useEffect(() => {
    const previousOverflow = document.body.style.overflow;

    document.body.style.overflow = "hidden";

    const handleKeyDown = (event) => {
      if (event.key === "Escape") {
        onClose();
      }
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [onClose]);

  return (
    <div className="photo-modal-backdrop" role="presentation" onClick={onClose}>
      <section
        className="photo-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="photo-modal-title"
        onClick={(event) => event.stopPropagation()}
      >
        <button
          type="button"
          className="photo-modal-close"
          aria-label="사진 상세 창 닫기"
          onClick={onClose}
        >
          ×
        </button>

        <div className="photo-modal-image-area">
          <img
            className="photo-modal-image"
            src={photo.imageUrl}
            alt={photo.title}
          />
        </div>

        <div className="photo-modal-details">
          <h2 id="photo-modal-title" className="photo-modal-title">
            {photo.title}
          </h2>

          {photo.location && (
            <p className="photo-modal-meta">
              <strong>장소</strong>
              <span>{photo.location}</span>
            </p>
          )}

          {photo.takenAt && (
            <p className="photo-modal-meta">
              <strong>촬영일</strong>
              <span>{photo.takenAt}</span>
            </p>
          )}

          {photo.description && (
            <p className="photo-modal-description">{photo.description}</p>
          )}
        </div>
      </section>
    </div>
  );
}

export default PhotoModal;
