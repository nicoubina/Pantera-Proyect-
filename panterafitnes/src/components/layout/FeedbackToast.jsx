"use client";

import { useEffect } from "react";
import { useAppData } from "@/context/AppDataContext";

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

  return (
    <div className={`feedback-toast ${feedback.tipo.toLowerCase()}`} role="alert">
      {feedback.mensaje}
    </div>
  );
}
