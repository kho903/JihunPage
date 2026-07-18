import { useState } from "react";
import { Link, useNavigate } from "react-router";

import { signupMember } from "../api/memberApi";

const initialFormData = {
  userid: "",
  userpwd: "",
  userpwdConfirm: "",
  username: "",
  tel: "",
  email: "",
};

const userIdRegex = /^[A-Za-z][A-Za-z0-9_]{5,14}$/;
/*
password
영문 최소 1개, 숫자 최소 1개, !@#$%^&*()_+=- 중 최소 1개, 전체 길이 8자 이상, 허용한 문자만 사용 가능
*/
const passwordRegex =
  /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+=-])[A-Za-z\d!@#$%^&*()_+=-]{8,}$/;
const nameRegex = /^[가-힣]{2,7}$/;
const telRegex = /^0\d{1,2}-\d{3,4}-\d{4}$/;
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function Signup() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState(initialFormData);
  const [errors, setErrors] = useState({});
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previousFormData) => {
      return {
        ...previousFormData,
        [name]: value,
      };
    });

    setErrors((previousErrors) => {
      return {
        ...previousErrors,
        [name]: "",
      };
    });

    setSubmitError("");
  };

  const validateForm = () => {
    const nextErrors = {};
    if (formData.userid.trim() === "")
      nextErrors.userid = "아이디를 입력해 주세요.";
    else if (!userIdRegex.test(formData.userid))
      nextErrors.userid =
        "아이디는 영문자로 시작하는 6~15자의 영문, 숫자, 언더바만 사용할 수 있습니다.";

    if (formData.userpwd === "")
      nextErrors.userpwd = "비밀번호를 입력해 주세요.";
    else if (!passwordRegex.test(formData.userpwd))
      nextErrors.userpwd =
        "비밀번호는 영문, 숫자, 특수문자(!@#$%^&*()_+=-)를 각각 하나 이상 포함하여 8자 이상이어야 합니다.";

    if (formData.userpwdConfirm === "")
      nextErrors.userpwdConfirm = "비밀번호를 다시 입력해 주세요.";
    else if (formData.userpwd !== formData.userpwdConfirm)
      nextErrors.userpwdConfirm = "비밀번호가 일치하지 않습니다.";

    if (formData.username.trim() === "")
      nextErrors.username = "이름을 입력해 주세요.";
    else if (!nameRegex.test(formData.username))
      nextErrors.username = "이름은 한글 2~7자로 입력해 주세요.";

    if (formData.tel.trim() === "")
      nextErrors.tel = "전화번호를 입력해 주세요.";
    else if (!telRegex.test(formData.tel))
      nextErrors.tel =
        "전화번호는 010-1234-5678과 같은 형식으로 입력해 주세요.";

    if (formData.email.trim() === "")
      nextErrors.email = "이메일을 입력해 주세요.";
    else if (!emailRegex.test(formData.email))
      nextErrors.email = "올바른 이메일 형식으로 입력해 주세요.";

    return nextErrors;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (isSubmitting) {
      return;
    }

    const nextErrors = validateForm();

    setErrors(nextErrors);
    setSubmitError("");

    // 프론트엔드에서 검증 실패시 API 요청을 보내지 않음.
    if (Object.keys(nextErrors).length > 0) return;

    setIsSubmitting(true);
    try {
      await signupMember(formData);
      setFormData(initialFormData);
      setErrors({});
      alert("회원가입이 완료되었습니다.");
      navigate("/login");
    } catch (error) {
      if (error.status === 400) {
        setErrors(error.data?.errors ?? {});
        setSubmitError(
          error.data?.message ?? "입력한 회원 정보를 확인해 주세요.",
        );
        return;
      }

      if (error.status === 409) {
        setErrors((previousErrors) => {
          return {
            ...previousErrors,
            userid: error.message,
          };
        });
        return;
      }

      if (
        error.status === undefined ||
        error.status === null ||
        error.status >= 500
      ) {
        setSubmitError(
          "서버에 연결할 수 없거나 서버 오류가 발생했습니다. 백엔드 서버를 확인해 주세요.",
        );
        return;
      }
      setSubmitError("회원가입 처리 중 알 수 없는 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="py-5">
      <div className="row justify-content-center">
        <div className="col-12 col-md-10 col-lg-7 col-xl-6">
          <div className="card border-0 shadow-sm">
            <div className="card-body p-4 p-md-5">
              <div className="text-center mb-4">
                <p className="text-primary fw-semibold mb-2">SIGN UP</p>

                <h1 className="h2 fw-bold mb-2">회원가입</h1>

                <p className="text-secondary mb-0">
                  JihunPage에서 사용할 계정 정보를 입력해 주세요.
                </p>
              </div>

              <form onSubmit={handleSubmit} noValidate>
                <div className="mb-3">
                  <label htmlFor="userid" className="form-label fw-semibold">
                    아이디
                  </label>

                  <input
                    type="text"
                    className={`form-control ${errors.userid ? "is-invalid" : ""}`}
                    id="userid"
                    name="userid"
                    value={formData.userid}
                    onChange={handleChange}
                    placeholder="아이디를 입력해 주세요"
                    autoComplete="username"
                    disabled={isSubmitting}
                  />

                  {errors.userid ? (
                    <div className="invalid-feedback">{errors.userid}</div>
                  ) : (
                    <div className="form-text">
                      영문자로 시작하는 6~15자의 영문, 숫자, 언더바만 사용할 수
                      있습니다.
                    </div>
                  )}
                </div>

                <div className="mb-3">
                  <label htmlFor="userpwd" className="form-label fw-semibold">
                    비밀번호
                  </label>

                  <input
                    type="password"
                    className={`form-control ${
                      errors.userpwd ? "is-invalid" : ""
                    }`}
                    id="userpwd"
                    name="userpwd"
                    value={formData.userpwd}
                    onChange={handleChange}
                    placeholder="비밀번호를 입력해 주세요"
                    autoComplete="new-password"
                    disabled={isSubmitting}
                  />

                  {errors.userpwd ? (
                    <div className="invalid-feedback">{errors.userpwd}</div>
                  ) : (
                    <div className="form-text">
                      영문, 숫자, 특수문자(!@#$%^&*()_+=-)를 각각 하나 이상
                      포함하여 8자 이상 입력해 주세요.
                    </div>
                  )}
                </div>

                <div className="mb-3">
                  <label
                    htmlFor="userpwdConfirm"
                    className="form-label fw-semibold"
                  >
                    비밀번호 확인
                  </label>

                  <input
                    type="password"
                    className={`form-control ${errors.userpwdConfirm ? "is-invalid" : ""}`}
                    id="userpwdConfirm"
                    name="userpwdConfirm"
                    value={formData.userpwdConfirm}
                    onChange={handleChange}
                    placeholder="비밀번호를 다시 입력해 주세요"
                    autoComplete="new-password"
                    disabled={isSubmitting}
                  />

                  {errors.userpwdConfirm && (
                    <div className="invalid-feedback">
                      {errors.userpwdConfirm}
                    </div>
                  )}
                </div>

                <div className="mb-3">
                  <label htmlFor="username" className="form-label fw-semibold">
                    이름
                  </label>

                  <input
                    type="text"
                    className={`form-control ${errors.username ? "is-invalid" : ""}`}
                    id="username"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    placeholder="이름을 입력해 주세요"
                    autoComplete="name"
                    disabled={isSubmitting}
                  />

                  {errors.username && (
                    <div className="invalid-feedback">{errors.username}</div>
                  )}
                </div>

                <div className="mb-3">
                  <label htmlFor="tel" className="form-label fw-semibold">
                    전화번호
                  </label>

                  <input
                    type="tel"
                    className={`form-control ${errors.tel ? "is-invalid" : ""}`}
                    id="tel"
                    name="tel"
                    value={formData.tel}
                    onChange={handleChange}
                    placeholder="010-1234-5678"
                    autoComplete="tel"
                    disabled={isSubmitting}
                  />

                  {errors.tel && (
                    <div className="invalid-feedback">{errors.tel}</div>
                  )}
                </div>

                <div className="mb-4">
                  <label htmlFor="email" className="form-label fw-semibold">
                    이메일
                  </label>

                  <input
                    type="email"
                    className={`form-control ${errors.email ? "is-invalid" : ""}`}
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    placeholder="example@email.com"
                    autoComplete="email"
                    disabled={isSubmitting}
                  />

                  {errors.email && (
                    <div className="invalid-feedback">{errors.email}</div>
                  )}
                </div>

                {submitError && (
                  <div className="alert alert-danger" role="alert">
                    {submitError}
                  </div>
                )}
                <button
                  type="submit"
                  className="btn btn-primary w-100 py-2"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? "회원가입 처리 중..." : "회원가입"}
                </button>
              </form>

              <p className="text-center text-secondary mt-4 mb-0">
                이미 계정이 있으신가요?{" "}
                <Link to="/login" className="fw-semibold text-decoration-none">
                  로그인
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default Signup;
