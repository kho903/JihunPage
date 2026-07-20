const skillGroups = [
  {
    id: "backend",
    title: "Backend",
    description: "서버, 비즈니스 로직과 REST API 개발에 사용하는 기술입니다.",
    skills: [
      {
        name: "Java",
        status: "Experienced",
      },
      {
        name: "Spring Framework",
        status: "Experienced",
      },
      {
        name: "Spring Boot",
        status: "Experienced",
      },
      {
        name: "Spring Data JPA",
        status: "Experienced",
      },
      {
        name: "Python",
        status: "Experienced",
      },
      {
        name: "Django",
        status: "Experienced",
      },
    ],
  },
  {
    id: "frontend",
    title: "Frontend",
    description: "웹 화면 구성과 사용자 인터페이스 개발에 사용하는 기술입니다.",
    skills: [
      {
        name: "HTML/CSS",
        status: "Experienced",
      },
      {
        name: "JavaScript",
        status: "Experienced",
      },
      {
        name: "React",
        status: "Learning",
      },
      {
        name: "Bootstrap",
        status: "Experienced",
      },
    ],
  },
  {
    id: "database",
    title: "Database",
    description: "데이터를 저장하고 조회하기 위해 사용한 데이터베이스입니다.",
    skills: [
      {
        name: "MySQL",
        status: "Experienced",
      },
      {
        name: "Oracle",
        status: "Experienced",
      },
      {
        name: "MongoDB",
        status: "Experienced",
      },
      {
        name: "Redis",
        status: "Planned",
      },
    ],
  },
  {
    id: "devops",
    title: "DevOps & Tools",
    description: "개발, 협업, 배포 환경을 구성하기 위한 기술과 도구입니다.",
    skills: [
      {
        name: "Git",
        status: "Experienced",
      },
      {
        name: "GitHub",
        status: "Experienced",
      },
      {
        name: "Docker",
        status: "Experienced",
      },
      {
        name: "Nginx",
        status: "Planned",
      },
      {
        name: "AWS",
        status: "Experienced",
      },
      {
        name: "GitHub Actions",
        status: "Planned",
      },
    ],
  },
];

const statusClassMap = {
  Experienced: "skill-status-experienced",
  Learning: "skill-status-learning",
  Planned: "skill-status-planned",
};

function Skills() {
  return (
    <section id="skills" className="py-5 border-top">
      <div className="mb-5">
        <p className="text-primary fw-semibold mb-2">SKILLS</p>

        <h2 className="display-6 fw-bold mb-3">경험하고 학습하고 있는 기술</h2>

        <p className="text-secondary mb-0">
          실제 사용 경험과 현재 학습 상태를 구분하여 정리했습니다.
        </p>
      </div>
      <div className="row g-4">
        {skillGroups.map((group) => {
          return (
            <div className="col-lg-6" key={group.id}>
              <article className="border rounded-3 p-4 h-100">
                <h3 className="h4 fw-bold mb-2">{group.title}</h3>
                <p className="text-secondary mb-4">{group.description}</p>

                <div className="d-flex flex-wrap gap-2">
                  {group.skills.map((skill) => {
                    return (
                      <div
                        className="border rounded-pill px-3 py-2"
                        key={skill.name}
                      >
                        <span className="fw-semibold me-2">{skill.name}</span>
                        <span
                          className={`skill-status ${statusClassMap[skill.status]}`}
                        >
                          {skill.status}
                        </span>
                      </div>
                    );
                  })}
                </div>
              </article>
            </div>
          );
        })}
      </div>
    </section>
  );
}

export default Skills;
