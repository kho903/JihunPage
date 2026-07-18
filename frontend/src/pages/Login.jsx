import { useState } from "react";
import { Link, useNavigate } from "react-router";

import { useAuth } from "../context/useAuth";

const initialFormData = {
  userid: "",
  userpwd: "",
};

function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();

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

    if (formData.userid.trim() === "") {
      nextErrors.userid = "아이디를 입력해 주세요.";
    }

    if (formData.userpwd === "") {
      nextErrors.userpwd = "비밀번호를 입력해 주세요.";
    }

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

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      await login(formData);

      setFormData(initialFormData);
      setErrors({});

      navigate("/", { replace: true });
    } catch (error) {
      if (error.status === 400) {
        setErrors(error.data?.error ?? {});
        setSubmitError(
          error.data?.message ?? "입력한 로그인 정보를 확인해 주세요.",
        );
        return;
      }

      if (error.status === 401) {
        setSubmitError(
          error.message ?? "아이디 또는 비밀번호가 올바르지 않습니다.",
        );

        setFormData((previousFormData) => {
          return {
            ...previousFormData,
            userpwd: "",
          };
        });

        return;
      }

      if (error.status === null || error.status >= 500) {
        setSubmitError(
          "서버에 연결할 수 없거나 서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
        );
        return;
      }

      setSubmitError("로그인 처리 중 알 수 없는 오류가 발생했습니다.");
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
                <p className="text-primary fw-semibold mb-2">LOGIN</p>

                <h1 className="h2 fw-bold mb-2">로그인</h1>

                <p className="text-secondary mb-0">
                  아이디와 비밀번호를 입력해 주세요.
                </p>
              </div>

              <form onSubmit={handleSubmit} noValidate>
                <div className="mb-3">
                  <label htmlFor="userid" className="form-label fw-semibold">
                    아이디
                  </label>

                  <input
                    type="text"
                    className={`form-control ${
                      errors.userid ? "is-invalid" : ""
                    }`}
                    id="userid"
                    name="userid"
                    value={formData.userid}
                    onChange={handleChange}
                    placeholder="아이디를 입력해 주세요"
                    autoComplete="username"
                  />

                  {errors.userid && (
                    <div className="invalid-feedback">{errors.userid}</div>
                  )}
                </div>

                <div className="mb-4">
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
                    autoComplete="current-password"
                    disabled={isSubmitting}
                  />

                  {errors.userpwd && (
                    <div className="invalid-feedback">{errors.userpwd}</div>
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
                  {isSubmitting ? "로그인 처리 중..." : "로그인"}
                </button>
              </form>

              <p className="text-center text-secondary mt-4 mb-0">
                아직 계정이 없으신가요?{" "}
                <Link to="/signup" className="fw-semibold text-decoration-none">
                  회원가입
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default Login;
