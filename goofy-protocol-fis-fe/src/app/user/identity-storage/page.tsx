'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {getAllUserIdentities, getKeypair} from "@/libs/auth-store";
import {useEffect, useState} from "react";
import {goPath} from "@/libs/go-path";
import {getMyHandle, isUser} from "@/libs/auth";
import {deleteAuth, getAuth, postAuth} from "@/libs/req";
import {IdentityStorageEntryDto, MyIdentityEntryQuotasDto} from "@/libs/dtos";
import {
    asymmSignStr,
    deriveHandleFromPublicSplitKey,
    generateAsymmKeypair, secretSymmKeyFromFullKey,
    serializeFullKeypair,
    symmEncryptObj
} from "@/libs/crypto";
import {AsymmCryptoType, AsymmFullJsonKeypair} from "@/libs/crypto-types";

export default function Page() {
    const [userHandle, setUserHandle] = useState<string | null>(null);
    const [identityEntries, setIdentityEntries] = useState<IdentityStorageEntryDto[]>([]);
    const [quotas, setQuotas] = useState<MyIdentityEntryQuotasDto | null>(null);

    useEffect(() => {(async () => {
        if (userHandle == null) {
            if (!(await isUser())) {
                goPath("/guest/login");
                return;
            }

            setUserHandle(await getMyHandle());
            await refresh();
        }
        })();
    });

    async function refresh() {
        await getQuotas();
        await getIdentities();
    }

    async function getQuotas() {
        const quotas: MyIdentityEntryQuotasDto = await getAuth("/api/identity-storage/quotas");
        setQuotas(quotas);
    }

    async function getIdentities() {
        setIdentityEntries(await getAllUserIdentities());
    }

    async function createIdentity() {
        const type = prompt("Enter type of keypair to generate:", "EC_C25519");
        if (type == null)
            return;

        const name = prompt("Enter a name/note for the identity. Can be blank");
        if (name == null)
            return;

        const keypair = await generateAsymmKeypair(AsymmCryptoType.fromValue(type));
        const handle = await deriveHandleFromPublicSplitKey(keypair.pub);
        const serializedKeypair: AsymmFullJsonKeypair = serializeFullKeypair(keypair);

        // Encrypt and Sign
        const myPrivSecret = await secretSymmKeyFromFullKey(await getKeypair());
        const encKeypair = await symmEncryptObj(serializedKeypair, myPrivSecret);
        const encKeySig = await asymmSignStr(encKeypair, keypair.priv);

        // Create Entry Object and Submit
        const newEntry: IdentityStorageEntryDto = {
            handle: handle,
            name: name,
            pubSplitKey: keypair.pub.serialize(),
            encKeypairEntry: encKeypair,
            encKeypairEntrySignature: encKeySig
        };

        try {
            await postAuth("/api/identity-storage", newEntry);
        } catch (e) {
            console.log(e);
            alert("Failed to create identity: " + (e as Error).message);
        }
        await refresh();
    }

    async function deleteIdentity(handle: string) {
        await deleteAuth("/api/identity-storage/" + encodeURIComponent(handle));
        await refresh();
    }

    // TODO: Styling
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Identity Storage List</h2>

                <br/>
                <p>
                    Hello, {userHandle}!<br/>
                    Quota: ({quotas?.currentEntryCount} / {quotas?.maxEntryCount})<br/>
                    These are your Identities:
                </p>

                <br/>
                <ul>
                    {identityEntries.map((entry) => (<li key={entry.handle}>
                            <span>{entry.name} - {entry.handle}</span> (Size: {entry.pubSplitKey.length} / {entry.encKeypairEntry.length})
                            <span> </span>
                            <Link href={`/user/service-entry-list#${entry.handle}`}>Manage</Link>
                            <span> </span>
                            <button onClick={() => {deleteIdentity(entry.handle).then()}}>Delete</button>
                        </li>
                    ))}
                </ul>
                <br/>

                <br/><hr/><br/>

                <button onClick={refresh}>Refresh</button><br/>
                <button onClick={createIdentity}>Create new Identity</button><br/>

                <br/><hr/><br/>

                <div className={styles.MainButtons}>
                    <Link href="/user/home">Home</Link>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
