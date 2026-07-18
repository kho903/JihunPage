import { Link } from "react-router";

function Hero() {
  return (
    <section className="py-5">
      <div className="container">
        <div className="row align-items-center py-5">
          <div className="col-lg-8">
            <p className="text-primary fw-semibold mb-2">
              Java & Spring Backend Developer
            </p>

            <h1>
              안녕하세요. <br />
              백엔드 개발자 김지훈입니다.
            </h1>

            <p>
              현재 html/css/js 순으로 학습 후 React로 미니 프로젝트를
              진행중입니다. 이후 백엔드를 연결해 풀스택으로 전환할 예정입니다.
            </p>

            <div className="d-flex flex-wrap gap-2">
              <a href="#about" className="btn btn-primary btn-lg">
                About Me
              </a>

              <Link to="/gallery" className="btn btn-outline-primary btn-lg">
                View Gallery
              </Link>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default Hero;
