import { Providers } from "@/app/providers";
import "@/styles/globals.css";

export const metadata = {
  title: "Pantera Fitness",
  description: "MVP frontend simulado de Pantera Fitness"
};

export default function RootLayout({ children }) {
  return (
    <html lang="es">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
