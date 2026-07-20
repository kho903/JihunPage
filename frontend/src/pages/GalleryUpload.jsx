import { useEffect, useState } from "react";
import { Navigate, useNavigate } from "react-router";

import { uploadGalleryPhoto } from "../api/galleryApi";
import { useAuth } from "../context/useAuth";

import "./css/GalleryUpload.css";

const MAX_IMAGE_SIZE = 5 * 1024 * 1024;

const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];

function GalleryUpload() {
  const navigate = useNavigate();
  const { currentMember, isAuthLoading } = useAuth();

  const [form, setForm] = useState({
    image: null,
    title: "",
    description: "",
    location: "",
    takenAt: "",
  });

  const [previewUrl, setPreviewUrl] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  const handleTextChange = (event) => {
    const { name, value } = event.target;

    setForm((previousForm) => ({
      ...previousForm,
      [name]: value,
    }));
  };

  const handleImageChange = (event) => {
    const image = event.target.files?.[0];
    setError("");
    if (!image) {
      setForm((previousForm) => ({
        ...previousForm,
        image: null,
      }));

      setPreviewUrl("");
      return;
    }

    if (!ALLOWED_IMAGE_TYPES.includes(image.type)) {
      event.target.value = "";

      setForm((previousForm) => ({
        ...previousForm,
        image: null,
      }));

      setPreviewUrl("");
      setError("JPEG, PNG, WebP 형식의 이미지만 선택할 수 있습니다.");
      return;
    }

    if (image.size > MAX_IMAGE_SIZE) {
      event.target.value = "";

      setForm((previousForm) => ({
        ...previousForm,
        image: null,
      }));

      setPreviewUrl("");
      setError("이미지 파일은 5MB 이하만 선택할 수 있습니다.");
      return;
    }

    const nextPreviewUrl = URL.createObjectURL(image);

    setForm((previousForm) => ({
      ...previousForm,
      image,
    }));

    setPreviewUrl(nextPreviewUrl);
  };

  const handleCancel = () => {
    navigate(-1);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    if (!form.image) {
      setError("업로드할 이미지를 선택해 주세요.");
      return;
    }

    if (!form.title.trim()) {
      setError("사진 제목을 입력해 주세요.");
      return;
    }

    const formData = new FormData();
    formData.append("image", form.image);
    formData.append("title", form.title.trim());

    if (form.description.trim()) {
      formData.append("description", form.description.trim());
    }

    if (form.location.trim()) {
      formData.append("location", form.location.trim());
    }

    if (form.takenAt) {
      formData.append("takenAt", form.takenAt);
    }

    setSubmitting(true);

    try {
      await uploadGalleryPhoto(formData);

      navigate(`/members/${encodeURIComponent(currentMember.userid)}/gallery`, {
        replace: true,
      });
    } catch (requestError) {
      if (requestError.status === 401) {
        setError("로그인이 만료되었습니다. 다시 로그인해 주세요.");
      } else if (requestError.status === 413) {
        setError("이미지 파일은 5MB 이하만 업로드할 수 있습니다.");
      } else {
        setError(requestError.message || "사진을 업로드하지 못했습니다.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (isAuthLoading) {
    return (
      <section className="gallery-upload-page text-center">
        <p className="text-secondary mb-0">
          로그인 정보를 확인하는 중입니다...
        </p>
      </section>
    );
  }

  if (!currentMember) {
    return <Navigate to="/login" replace />;
  }

  return (
    <section className="gallery-upload-page">
      <header className="gallery-upload-header">
        <h1 className="gallery-upload-title">사진 등록</h1>

        <p className="gallery-upload-description">
          갤러리에 새로운 사진을 등록합니다.
        </p>
      </header>

      <form className="gallery-upload-form" onSubmit={handleSubmit}>
        <div className="gallery-upload-content">
          <div className="gallery-upload-image-section">
            <label className="form-label" htmlFor="gallery-image">
              이미지
            </label>

            <input
              id="gallery-image"
              className="form-control"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              disabled={submitting}
              onChange={handleImageChange}
            />

            <div className="form-text">JPEG, PNG, WebP 형식 · 최대 5MB</div>

            <div className="gallery-upload-preview">
              {previewUrl ? (
                <img src={previewUrl} alt="선택한 사진 미리보기" />
              ) : (
                <p className="gallery-upload-preview-empty">
                  이미지를 선택하면 미리보기가 표시됩니다.
                </p>
              )}
            </div>
          </div>

          <div className="gallery-upload-fields">
            <div>
              <label className="form-label" htmlFor="gallery-title">
                제목
              </label>

              <input
                id="gallery-title"
                name="title"
                className="form-control"
                type="text"
                maxLength={100}
                placeholder="사진 제목을 입력해 주세요."
                value={form.title}
                disabled={submitting}
                onChange={handleTextChange}
              />
            </div>

            <div>
              <label className="form-label" htmlFor="gallery-description">
                설명
              </label>

              <textarea
                id="gallery-description"
                name="description"
                className="form-control"
                rows={5}
                maxLength={1000}
                placeholder="사진에 대한 설명을 입력해 주세요."
                value={form.description}
                disabled={submitting}
                onChange={handleTextChange}
              />
            </div>

            <div>
              <label className="form-label" htmlFor="gallery-location">
                장소
              </label>

              <input
                id="gallery-location"
                name="location"
                className="form-control"
                type="text"
                maxLength={100}
                placeholder="사진을 촬영한 장소"
                value={form.location}
                disabled={submitting}
                onChange={handleTextChange}
              />
            </div>

            <div>
              <label className="form-label" htmlFor="gallery-taken-at">
                촬영일
              </label>

              <input
                id="gallery-taken-at"
                name="takenAt"
                className="form-control"
                type="date"
                value={form.takenAt}
                disabled={submitting}
                onChange={handleTextChange}
              />
            </div>
          </div>
        </div>

        {error && (
          <p className="gallery-upload-error" role="alert">
            {error}
          </p>
        )}

        <div className="gallery-upload-actions">
          <button
            type="button"
            className="btn btn-outline-secondary"
            disabled={submitting}
            onClick={handleCancel}
          >
            취소
          </button>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting}
          >
            {submitting ? "업로드 중..." : "사진 등록"}
          </button>
        </div>
      </form>
    </section>
  );
}

export default GalleryUpload;
