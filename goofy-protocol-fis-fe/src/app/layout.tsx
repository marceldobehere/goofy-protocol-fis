import { Geist, Geist_Mono } from "next/font/google";
import styles from "./layout.module.css";
import "./globals.css";
import {Metadata} from "next";
import SettingsModal from "./components/settings-modal/component";


const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Goofy FIS Frontend",
  description: "Reference Frontend Implementation for the FIS",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
    return (
    <html lang="en" className={`${geistSans.variable} ${geistMono.variable}`}>
      <body>
      {children}

      <button id={styles.FloatingSettingsButton} command={"show-modal"} commandfor={styles.FloatingSettings}>&#x2699;</button>
      <dialog id={styles.FloatingSettings}>
          <button command={"close"} commandfor={styles.FloatingSettings}>X</button>
          <SettingsModal></SettingsModal>
      </dialog>
      </body>
    </html>
  );
}
