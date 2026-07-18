import { Link } from "react-router";

function Signup() {
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
                <p className="text-primary fw-semibold mb-2">SIGN UP</p>

                <h1 className="h2 fw-bold mb-2">회원가입</h1>

                <p className="text-secondary mb-0">
                  JihunPage에서 사용할 계정 정보를 입력해 주세요.
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
                    placeholder="아이디를 입력해 주세요"
                    autoComplete="username"
                  />

                  <div className="form-text">
                    영문자로 시작하는 6~15자의 영문, 숫자, 언더바만 사용할 수
                    있습니다.
                  </div>
                </div>

                <div className="mb-3">
                  <label htmlFor="userpwd" className="form-label fw-semibold">
                    비밀번호
                  </label>

                  <input
                    type="password"
                    className="form-control"
                    id="userpwd"
                    name="userpwd"
                    placeholder="비밀번호를 입력해 주세요"
                    autoComplete="new-password"
                  />

                  <div className="form-text">
                    영문, 숫자, 특수문자를 각각 하나 이상 포함하여 8자 이상
                    입력해 주세요.
                  </div>
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
                    className="form-control"
                    id="userpwdConfirm"
                    name="userpwdConfirm"
                    placeholder="비밀번호를 다시 입력해 주세요"
                    autoComplete="new-password"
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="username" className="form-label fw-semibold">
                    이름
                  </label>

                  <input
                    type="text"
                    className="form-control"
                    id="username"
                    name="username"
                    placeholder="이름을 입력해 주세요"
                    autoComplete="name"
                  />
                </div>

                <div className="mb-3">
                  <label htmlFor="tel" className="form-label fw-semibold">
                    전화번호
                  </label>

                  <input
                    type="tel"
                    className="form-control"
                    id="tel"
                    name="tel"
                    placeholder="010-1234-5678"
                    autoComplete="tel"
                  />
                </div>

                <div className="mb-4">
                  <label htmlFor="email" className="form-label fw-semibold">
                    이메일
                  </label>

                  <input
                    type="email"
                    className="form-control"
                    id="email"
                    name="email"
                    placeholder="example@email.com"
                    autoComplete="email"
                  />
                </div>

                <button type="submit" className="btn btn-primary w-100 py-2">
                  회원가입
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
