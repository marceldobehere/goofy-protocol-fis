'use client';

import styles from "./page.module.css";
import Link from "next/link";

export default function Page() {
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Goofy FIS Frontend</h2>

                <p className={styles.Introduction}>
                    A reference implementation of the FIS Frontend for the Goofy Protocol.<br/><br/>
                    For more information, visit <a href={"https://github.com/marceldobehere/goofy-protocol-fis"} target={"_blank"}>this page</a>!
                </p>

                <div className={styles.MainButtons}>
                    <Link href={"/guest/login"}>Login</Link>
                    <Link href={"/guest/register"}>Register</Link>
                    <Link href={"/user/home"}>Home</Link>
                    <Link href={"/guest/layout-test"}>Layout Test</Link>
                </div>
            </div>
        </main>
    );
}
