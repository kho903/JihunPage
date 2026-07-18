const contactItems = [
  {
    id: "email",
    title: "Email",
    description: "프로젝트와 개발 관련 연락은 이메일로 부탁드립니다.",
    linkText: "Send Email",
    href: "mailto:gmldnr2222@naver.com",
  },
  {
    id: "github",
    title: "GitHub",
    description: "학습 기록과 프로젝트 코드를 확인할 수 있습니다.",
    linkText: "View GitHub",
    href: "https://github.com/kho903",
  },
];

function Contact() {
  return (
    <section id="contact" className="py-5 border-top">
      <div className="row align-items-center g-5">
        <div className="col-lg-5">
          <p className="text-primary fw-semibold mb-2">CONTACT</p>

          <h2 className="display-6 fw-bold mb-3">Let's Talk</h2>

          <p className="text-secondary mb-0">
            프로젝트, 개발 공부와 관련된 이야기를 환영합니다. 이메일이나
            GitHub를 통해 연락할 수 있습니다.
          </p>
        </div>

        <div className="col-lg-7">
          <div className="row g-3">
            {contactItems.map((contact) => {
              const isExternalLink = contact.href.startsWith("https://");

              return (
                <div className="col-md-6" key={contact.id}>
                  <article className="border rounded-3 p-4 h-100">
                    <h3 className="h4 fw-bold mb-3">{contact.title}</h3>

                    <p className="text-secondary mb-4">{contact.description}</p>

                    <a
                      href={contact.href}
                      className="btn btn-outline-primary"
                      target={isExternalLink ? "_blank" : undefined}
                      rel={isExternalLink ? "noopener noreferrer" : undefined}
                    >
                      {contact.linkText}
                    </a>
                  </article>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}

export default Contact;
