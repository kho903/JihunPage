import { Navigate } from "react-router";

import { useAuth } from "../../context/useAuth";

function MyGalleryRedirect() {
  const { currentMember, isAuthLoading } = useAuth();

  if (isAuthLoading) {
    return (
      <section className="py-5 text-center">
        <p className="text-secondary mb-0">
          로그인 정보를 확인하는 중입니다...
        </p>
      </section>
    );
  }

  if (!currentMember) {
    return <Navigate to="/login" replace />;
  }

  const galleryPath = `/members/${encodeURIComponent(currentMember.userid)}/gallery`;

  return <Navigate to={galleryPath} replace />;
}

export default MyGalleryRedirect;
