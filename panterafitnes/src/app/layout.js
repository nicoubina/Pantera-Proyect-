import { Providers } from "@/app/providers";
import "@/styles/globals.css";

// Al usar un ícono nuevo de Material Symbols, agregarlo a esta lista (orden alfabético).
const ICONS = [
  "arrow_forward", "badge", "block", "calendar_month", "check_circle", "checklist",
  "chevron_right", "close", "error", "event", "event_available", "exercise", "expand_more",
  "fitness_center", "gavel", "group", "groups", "home", "hourglass_empty", "lock", "logout",
  "mail", "monitoring", "notifications", "person", "photo_camera", "qr_code", "qr_code_scanner",
  "schedule", "shield", "sports", "star", "update", "videocam_off", "warning"
];
const ICONS_URL = "https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0"
  + `&icon_names=${[...ICONS].sort().join(",")}&display=block`;

export const viewport = { themeColor: "#070707" };

export const metadata = {
  title: "Pantera Fitness",
  description: "Gestión de entrenamiento y clases de Pantera Fitness"
};

export default function RootLayout({ children }) {
  return (
    <html lang="es">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Anybody:wght@100..900&family=Hanken+Grotesk:wght@100..900&display=swap"
          rel="stylesheet"
        />
        {/* Solo los íconos que usa la app (~5 KB en vez de ~4 MB de la fuente completa). */}
        <link href={ICONS_URL} rel="stylesheet" />
      </head>
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
