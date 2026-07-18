const aboutItems = [
  {
    title: "Current",
    content: "K-Move 일본 IT 취업 과정을 수강하고 있습니다.",
  },
  {
    title: "Goal",
    content: "일본에서 Java와 Spring 기반의 백엔드 개발자로 취업하는 것입니다.",
  },
  {
    title: "Background",
    content: "수학을 전공하고 융합데이터공학을 복수전공했습니다.",
  },
  {
    title: "Tech Stack (Planned)",
    content:
      "Java 21, Spring Boot, React(JS), MySQL, Docker, AWS, Nginx, GitHub Actions",
  },
];

function About() {
  return (
    <section id="about" className="py-5 border-top">
      <div className="row align-items-start g-5">
        <div className="col-lg-5">
          <p className="text-primary fw-semibold mb-2">ABOUT ME</p>

          <h2 className="display-6 fw-bold mb-3">
            새로운 기술을 꾸준히 배우는 개발자
          </h2>

          <p className="text-secondary">
            프론트엔드와 백엔드를 함께 이해하면서, 사용자에게 필요한 서비스를
            안정적으로 구현할 수 있는 개발자를 목표로 하고 있습니다.
          </p>
        </div>

        <div className="col-lg-7">
          <div className="row g-3">
            {aboutItems.map((item) => {
              return (
                <div className="col-md-6" key={item.title}>
                  <div className="border rounded-3 p-4 h-100">
                    <h3 className="h5 fw-bold">{item.title}</h3>
                    <p className="text-secondary mb-0">{item.content}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}

export default About;
