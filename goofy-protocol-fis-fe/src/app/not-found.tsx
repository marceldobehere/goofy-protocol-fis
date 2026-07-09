'use client';

import styles from "./not-found.module.css";
import Link from "next/link";

export default function Page() {
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Not Found</h2>

                <p className={styles.Body}>
                    The page you&apos;re looking for doesn&apos;t exist!

                </p>

                <div className={styles.MainButtons}>
                    <Link href={"/"}>Index</Link>
                    <Link href={"/guest/login"}>Login</Link>
                    <Link href={"/guest/register"}>Register</Link>
                </div>
            </div>
        </main>
    );
}
