const projectItems = [
  {
    id: 1,
    title: "Bancow Website Renewal",
    period: "2022.01 - 2022.02",
    type: "Company-Connected Project",
    description:
      "Wix로 운영되던 Bancow 사이트를 Spring Boot와 React 기반으로 리뉴얼하고 백오피스를 개발한 프로젝트입니다.",
    roles: [
      "백오피스 기능 기획 및 백엔드 개발",
      "농가 입점 및 문의 기능 REST API 구현",
      "JUnit 테스트 코드와 Spring REST Docs API 문서 작성",
      "프론트엔드 개발자 및 UI/UX 디자이너와 협업",
    ],
    stack: [
      "Spring Boot",
      "Spring Data JPA",
      "Spring REST Docs",
      "JUnit",
      "Docker",
      "Naver Cloud",
    ],
    githubUrl: "https://github.com/kho903/bancow-back/tree/develop",
  },
  {
    id: 2,
    title: "Musicgram",
    period: "2021.01 - 2021.02",
    type: "Team Project",
    description:
      "Instagram의 게시글 구조에 음악 콘텐츠 기능을 결합한 Spring 기반 웹 애플리케이션입니다.",
    roles: [
      "YouTube URL을 파싱하여 동영상 콘텐츠 출력",
      "음악 게시글 등록·조회·수정·삭제 기능 구현",
      "Spring MVC 기반 서버 기능 개발",
      "백엔드 개발자 4명으로 구성된 팀에서 협업",
    ],
    stack: ["Java", "Spring Framework", "MyBatis", "Oracle", "Bootstrap"],
    githubUrl: "https://github.com/kho903/musicgram",
  },
  {
    id: 3,
    title: "Smart Flood Prevention System",
    period: "2020.08 - 2020.09",
    type: "Data Team Project",
    description:
      "침수 사고 데이터를 활용하여 위험 상황을 예측하고 시각화하는 웹 시스템입니다.",
    roles: [
      "Django 기반 백엔드 및 웹 화면 구축",
      "D3.js와 Chart.js를 활용한 데이터 시각화",
      "머신러닝·데이터 분석·인프라 담당자와 협업",
      "프로젝트 평가에서 과학기술정보통신부장관상 수상",
    ],
    stack: ["Python", "Django", "D3.js", "Chart.js", "SQLite"],
    githubUrl: "https://github.com/pcrmcw0486/dataflood",
  },
];

function Projects() {
  return (
    <section id="projects" className="py-5 border-top">
      <div className="mb-5">
        <p className="text-primary fw-semibold mb-2">PROJECTS</p>

        <h2 className="display-6 fw-bold mb-3">직접 참여한 프로젝트</h2>

        <p className="text-secondary mb-0">
          팀 프로젝트에서 담당한 기능과 사용 기술을 정리했습니다.
        </p>
      </div>

      <div className="row g-4">
        {projectItems.map((project) => {
          return (
            <div className="col-lg-4" key={project.id}>
              <article className="card h-100 shadow-sm">
                <div className="card-body d-flex flex-column p-4">
                  <div className="mb-3">
                    <span className="badge text-bg-primary me-2">
                      {project.type}
                    </span>

                    <span className="text-secondary small">
                      {project.period}
                    </span>
                  </div>

                  <h3 className="h4 fw-bold mb-3">{project.title}</h3>

                  <p className="text-secondary">{project.description}</p>

                  <h4 className="h6 fw-bold mt-2">My Role</h4>

                  <ul className="text-secondary ps-3 mb-4">
                    {project.roles.map((role) => {
                      return (
                        <li className="mb-2" key={role}>
                          {role}
                        </li>
                      );
                    })}
                  </ul>

                  <div className="d-flex flex-wrap gap-2 mb-4">
                    {project.stack.map((technology) => {
                      return (
                        <span
                          className="badge text-bg-light border text-dark"
                          key={technology}
                        >
                          {technology}
                        </span>
                      );
                    })}
                  </div>

                  <a
                    href={project.githubUrl}
                    className="btn btn-outline-primary mt-auto"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    View GitHub
                  </a>
                </div>
              </article>
            </div>
          );
        })}
      </div>
    </section>
  );
}

export default Projects;
