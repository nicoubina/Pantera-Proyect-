export default function MetricCard({ label, value, detail, tone = "neutral", icon }) {
  return (
    <article className={`metric-card ${tone}`}>
      {icon ? (
        <span className="material-symbols-outlined" style={{ color: "var(--color-orange)", fontSize: "22px" }}>
          {icon}
        </span>
      ) : null}
      <span>{label}</span>
      <strong>{value}</strong>
      {detail ? <p>{detail}</p> : null}
    </article>
  );
}
