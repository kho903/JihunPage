export async function getPublicGallery(userid, signal) {
  const encodedUserid = encodeURIComponent(userid);

  const response = await fetch(`/api/members/${encodedUserid}/gallery`, {
    method: "GET",
    signal,
  });

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const error = new Error(data?.message || "갤러리를 불러오지 못했습니다.");

    error.status = response.status;
    error.code = data?.code;

    throw error;
  }

  return data;
}
