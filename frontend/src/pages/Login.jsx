import { useState } from "react";
import { Link } from "react-router";

const initialFormData = {
  userid: "",
  userpwd: "",
};

function Login() {
  const [formData, setFormData] = useState(initialFormData);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((previousFormData) => {
      return {
        ...previousFormData,
        [name]: value,
      };
    });
  };

  const handleSubmit = (event) => {
    event.preventDefault();
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

              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label htmlFor="userid" className="form-label fw-semibold">
                    아이디
                  </label>

                  <input
                    type="text"
                    className="form-control"
                    id="userid"
                    name="userid"
                    value={formData.userid}
                    onChange={handleChange}
                    placeholder="아이디를 입력해 주세요"
                    autoComplete="username"
                  />
                </div>

                <div className="mb-4">
                  <label htmlFor="userpwd" className="form-label fw-semibold">
                    비밀번호
                  </label>

                  <input
                    type="password"
                    className="form-control"
                    id="userpwd"
                    name="userpwd"
                    value={formData.userpwd}
                    onChange={handleChange}
                    placeholder="비밀번호를 입력해 주세요"
                    autoComplete="current-password"
                  />
                </div>

                <button type="submit" className="btn btn-primary w-100 py-2">
                  로그인
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
