'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {saveKeypair} from "@/libs/auth-store";
import {useEffect, useState} from "react";
import {goPath} from "@/libs/go-path";
import {getMyHandle, isUser} from "@/libs/auth";

export default function Page() {
    const [userHandle, setUserHandle] = useState<string | null>(null);

    useEffect(() => {(async () => {
        if (userHandle == null) {
            if (!(await isUser())) {
                goPath("/guest/login");
                return;
            }

            setUserHandle(await getMyHandle());
        }
        })();
    });

    async function logout() {
        await saveKeypair(null);
        setUserHandle(null);
    }

    // TODO: Styling
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Home</h2>

                <br/>
                <p>Hello, {userHandle}! This is the Home Page.</p>

                <br/><hr/><br/>

                <button onClick={logout}>Logout</button><br/>

                <div className={styles.MainButtons}>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
