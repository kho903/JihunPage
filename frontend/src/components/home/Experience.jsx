const experienceItems = [
  {
    id: 1,
    period: "2026.06 - 2026.12",
    category: "Training",
    title: "K-Move Japan IT Program",
    organization: "Japan IT Employment Training",
    details: [
      "일본 IT 취업을 목표로 HTML, CSS, JavaScript, React를 학습하고 있습니다.",
      "이후 Java, Spring Boot, 데이터베이스를 학습하고 팀 프로젝트를 진행할 예정입니다.",
    ],
  },
  {
    id: 2,
    period: "2024.11 - 2025.11",
    category: "Experience",
    title: "Japan Working Holiday",
    organization: "Japan",
    details: [
      "일본에서 약 1년 동안 생활하며 일본 문화와 생활 환경을 직접 경험했습니다.",
      "다양한 사람들과 소통하며 일본어 의사소통 능력과 현지 적응력을 키웠습니다.",
    ],
  },
  {
    id: 3,
    period: "2022.01 - 2022.02",
    category: "Project",
    title: "Bancow Website Renewal",
    organization: "Company-Connected Team Project",
    details: [
      "Spring Boot와 React를 활용한 사이트 리뉴얼 및 백오피스 개발에 참여했습니다.",
      "농가 입점과 문의 기능 API를 구현하고 테스트 코드 및 REST Docs API 문서를 작성했습니다.",
      "백엔드 개발자, 프론트엔드 개발자, UI/UX 디자이너와 협업했습니다.",
    ],
  },
  {
    id: 4,
    period: "2021.07 - 2022.02",
    category: "Training",
    title: "Fintech Backend Developer Program",
    organization: "FastCampus",
    details: [
      "Java와 Spring Boot를 기반으로 백엔드 개발 과정을 수료했습니다.",
      "기업 연계 프로젝트와 팀 활동을 통해 API 개발, 테스트 및 협업 과정을 경험했습니다.",
      "Hello. Megabyte 해커톤에서 장려상을 수상했습니다.",
    ],
  },
  {
    id: 5,
    period: "2021.02 - 2021.03",
    category: "Challenge",
    title: "42Seoul La Piscine",
    organization: "Innovation Academy",
    details: [
      "C 언어를 사용해 약 5주 동안 집중적인 프로그래밍 과정을 경험했습니다.",
      "동료 평가와 문제 해결 중심의 학습을 통해 스스로 학습하는 방법을 익혔습니다.",
    ],
  },
  {
    id: 6,
    period: "2020.12 - 2021.02",
    category: "Training",
    title: "Java Web Programming Program",
    organization: "Multicampus",
    details: [
      "Java, Spring Framework, MyBatis와 Oracle을 활용한 웹 개발을 학습했습니다.",
      "Musicgram 팀 프로젝트에서 YouTube URL 파싱과 음악 게시글 CRUD 기능을 구현했습니다.",
    ],
  },
  {
    id: 7,
    period: "2020.06 - 2020.09",
    category: "Project",
    title: "Smart Flood Accident Prevention System",
    organization: "Data Youth Campus",
    details: [
      "Django를 활용한 백엔드 구축과 D3.js, Chart.js 기반 데이터 시각화를 담당했습니다.",
      "머신러닝, 데이터 분석, 인프라 담당자와 함께 협업했습니다.",
      "프로젝트 평가에서 과학기술정보통신부장관상을 수상했습니다.",
    ],
  },
  {
    id: 8,
    period: "2019",
    category: "Activity",
    title: "Pirogramming",
    organization: "Web Development Club",
    details: [
      "HTML, CSS와 Django를 활용하며 처음으로 웹 개발을 경험했습니다.",
      "팀 단위 웹 개발을 통해 웹 서비스와 백엔드 개발에 관심을 갖게 되었습니다.",
    ],
  },
];

function Experience() {
  return (
    <section id="experience" className="py-5 border-top">
      <div className="mb-5">
        <p className="text-primary fw-semibold mb-2">EXPERIENCE</p>

        <h2 className="display-6 fw-bold mb-3">개발자로 성장해 온 과정</h2>

        <p className="text-secondary mb-0">
          교육, 프로젝트, 수상 및 일본 생활을 통해 쌓아 온 경험입니다.
        </p>
      </div>

      <div className="row g-4">
        {experienceItems.map((experience) => {
          return (
            <div className="col-lg-6" key={experience.id}>
              <article className="border rounded-3 p-4 h-100">
                <div className="d-flex flex-wrap align-items-center gap-2 mb-3">
                  <span className="home-tag">{experience.category}</span>

                  <span className="text-secondary fw-semibold">
                    {experience.period}
                  </span>
                </div>

                <h3 className="h4 fw-bold mb-2">{experience.title}</h3>

                <p className="text-primary fw-semibold mb-3">
                  {experience.organization}
                </p>

                <ul className="text-secondary mb-0 ps-3">
                  {experience.details.map((detail) => {
                    return (
                      <li className="mb-2" key={detail}>
                        {detail}
                      </li>
                    );
                  })}
                </ul>
              </article>
            </div>
          );
        })}
      </div>
    </section>
  );
}

export default Experience;
