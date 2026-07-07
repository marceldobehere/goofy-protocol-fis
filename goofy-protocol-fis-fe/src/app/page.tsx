'use client';

import styles from "./page.module.css";
import Link from "next/link";

export default function Home() {
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Goofy FIS Frontend</h2>

                <p className={styles.Introduction}>
                    A reference implementation of the FIS Frontend for the Goofy Protocol.
                </p>


                <div className={styles.MainButtons}>
                    <Link href={"/guest/login"}>Login</Link>
                    <Link href={"/guest/register"}>Register</Link>
                </div>
            </div>
        </main>
    );
}
