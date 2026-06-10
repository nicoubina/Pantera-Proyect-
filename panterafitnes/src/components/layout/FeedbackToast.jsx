"use client";

import { useEffect } from "react";
import { useAppData } from "@/context/AppDataContext";

const ICONS = {
  SUCCESS: "check_circle",
  WARNING: "warning",
  INFO: "warning",
  ERROR: "error"
};

export default function FeedbackToast() {
  const { feedback, setFeedback } = useAppData();

  useEffect(() => {
    if (!feedback) {
      return undefined;
    }

    const timeoutId = window.setTimeout(() => setFeedback(null), 4200);
    return () => window.clearTimeout(timeoutId);
  }, [feedback, setFeedback]);

  if (!feedback) {
    return null;
  }

  const icon = ICONS[feedback.tipo] || "warning";

  return (
    <div className={`feedback-toast ${feedback.tipo.toLowerCase()}`} role="alert">
      <span className="material-symbols-outlined">{icon}</span>
      <span style={{ flex: 1 }}>{feedback.mensaje}</span>
      <button
        type="button"
        onClick={() => setFeedback(null)}
        aria-label="Cerrar"
        style={{ background: "none", border: "none", cursor: "pointer", color: "inherit", padding: 0 }}
      >
        <span className="material-symbols-outlined" style={{ fontSize: "16px" }}>
          close
        </span>
      </button>
      <div
        className="toast-progress"
        style={{
          position: "absolute",
          bottom: 0,
          left: 0,
          height: "3px",
          background: "currentColor",
          opacity: 0.4,
          borderRadius: "0 0 8px 8px"
        }}
      />
    </div>
  );
}
