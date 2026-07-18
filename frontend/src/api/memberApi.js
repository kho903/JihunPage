export async function signupMember(memberData) {
  const response = await fetch("/api/members", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(memberData),
  });

  const responseData = await response.json().catch(() => null);

  if (!response.ok) {
    const error = new Error(
      responseData?.message ?? "회원가입 처리 중 오류가 발생했습니다.",
    );

    error.status = response.status;
    error.data = responseData;
    throw error;
  }

  return responseData;
}
