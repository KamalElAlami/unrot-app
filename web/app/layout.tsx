import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Focus Reset - Take the daily focus challenge",
  description: "A finite anti-brainrot challenge powered by five deliberate mind games.",
  icons: { icon: "/favicon.svg", shortcut: "/favicon.svg" },
  openGraph: {
    title: "Can your focus survive five minutes?",
    description: "Try today's Color Clash, then join the 7-day Focus Reset.",
    type: "website",
  },
  twitter: {
    card: "summary",
    title: "Focus Reset",
    description: "A finite daily focus challenge - not another feed.",
  },
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
