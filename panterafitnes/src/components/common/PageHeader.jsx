export default function PageHeader({ eyebrow, title, description }) {
  return (
    <header className="page-header">
      {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
      <h2>{title}</h2>
      {description ? <p className="muted">{description}</p> : null}
    </header>
  );
}
