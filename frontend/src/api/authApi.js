async function readResponseData(response) {
  return response.json().catch(() => null);
}

function createApiError(response, responseData, defaultMessage) {
  const error = new Error(responseData?.message ?? defaultMessage);

  error.status = response.status;
  error.data = responseData;

  return error;
}

export async function loginMember(loginData) {
  const response = await fetch("/api/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify(loginData),
  });

  const responseData = await readResponseData(response);

  if (!response.ok) {
    throw createApiError(
      response,
      responseData,
      "로그인 처리 중 오류가 발생했습니다.",
    );
  }
  return responseData;
}

export async function logoutMember() {
  const response = await fetch("/api/auth/logout", {
    method: "POST",
    credentials: "include",
  });

  if (!response.ok) {
    const responseData = await readResponseData(response);

    throw createApiError(
      response,
      responseData,
      "로그아웃 처리 중 오류가 발생했습니다.",
    );
  }
}

export async function getCurrentMember() {
  const response = await fetch("/api/auth/me", {
    method: "GET",
    credentials: "include",
  });

  const responseData = await readResponseData(response);

  if (response.status === 401) {
    return null;
  }

  if (!response.ok) {
    throw createApiError(
      response,
      responseData,
      "로그인 상태를 확인하는 중 오류가 발생했습니다.",
    );
  }

  return responseData;
}
