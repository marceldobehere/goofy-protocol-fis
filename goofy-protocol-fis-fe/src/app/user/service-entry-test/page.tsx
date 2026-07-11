'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {getKeypair} from "@/libs/auth-store";
import {useEffect, useState} from "react";
import {goPath} from "@/libs/go-path";
import {getMyHandle, isUser} from "@/libs/auth";
import {deleteFixedAuth, getAuth, getFixedAuth, postFixedAuth, putFixedAuth} from "@/libs/req";
import {IdentityStorageEntryDto, MyServiceEntryQuotasDto, ServiceEntryDto} from "@/libs/dtos";
import {
    asymmVerifyStr, checkPublicSplitKey,
    parseFullKeypair, parsePublicSplitKey, secretSymmKeyFromFullKey,
    symmDecryptObj
} from "@/libs/crypto";
import {AsymmFullJsonKeypair, AsymmFullKeyPair} from "@/libs/crypto-types";

export default function Page() {
    const [userHandle, setUserHandle] = useState<string | null>(null);
    const [identityKeypair, setIdentityKeypair] = useState<AsymmFullKeyPair | null>(null);
    const [identityHandle, setIdentityHandle] = useState<string | null>(null);

    const [serviceEntries, setServiceEntries] = useState<ServiceEntryDto[]>([]);
    const [quotas, setQuotas] = useState<MyServiceEntryQuotasDto | null>(null);

    useEffect(() => {(async () => {
        if (userHandle == null) {
            if (!(await isUser())) {
                goPath("/guest/login");
                return;
            }

            setUserHandle(await getMyHandle());
        }

        if (identityKeypair == null) {
            if (window.location.hash == "")
                goPath("/user/home");
            const fragmentHandle =  window.location.hash.slice(window.location.hash.lastIndexOf("#") + 1);
            // window.location.hash = "#" + fragmentHandle;

            try {
                const entryDto: IdentityStorageEntryDto = await getAuth("/api/identity-storage/" + encodeURIComponent(fragmentHandle));

                // Check Signature
                const sigCheck = await asymmVerifyStr(entryDto.encKeypairEntry, entryDto.encKeypairEntrySignature, parsePublicSplitKey(entryDto.pubSplitKey));
                if (!sigCheck) {
                    alert("Signature check failed for identity " + fragmentHandle);
                    return;
                }

                // Decrypt
                const myPrivSecret = await secretSymmKeyFromFullKey(await getKeypair());
                const decKeypair = await symmDecryptObj<AsymmFullJsonKeypair>(entryDto.encKeypairEntry, myPrivSecret);
                const keypair = parseFullKeypair(decKeypair);

                const checkValid = await checkPublicSplitKey(keypair.pub);
                if (!checkValid) {
                    alert("Decrypted keypair is invalid for identity " + fragmentHandle);
                    return;
                }

                setIdentityHandle(entryDto.handle);
                setIdentityKeypair(keypair);
            } catch (e) {
                console.log(e);
                alert(`Identity for ${fragmentHandle} not found`);
                goPath("/user/home");
                return;
            }
        }

        if (quotas == null) {
            await refresh();
        }
        })();
    });

    async function refresh() {
        await getQuotas();
        await getEntries();
    }

    async function getQuotas() {
        if (identityKeypair == null)
            return;

        const quotas: MyServiceEntryQuotasDto = await getFixedAuth("/api/service-entry/quotas", identityKeypair);
        setQuotas(quotas);
    }

    async function getEntries() {
        if (identityKeypair == null)
            return;

        const entries: ServiceEntryDto[] = await getFixedAuth("/api/service-entry", identityKeypair);
        setServiceEntries(entries);
    }

    async function createEntry() {
        if (identityKeypair == null)
            return;

        const name = prompt("Enter a name for the Service Entry");
        if (name == null)
            return;

        const usedService = prompt("Enter the name or url of the Service. Can be blank");
        if (usedService == null)
            return;

        const newEntry: ServiceEntryDto = {
            name: name,
            usedService: usedService,
            uuid: "" // Keep blank
        }

        try {
            await postFixedAuth("/api/service-entry", newEntry, identityKeypair);
        } catch (e) {
            console.log(e);
            alert("Failed to create Service Entry: " + (e as Error).message);
        }
        await refresh();
    }

    async function deleteEntry(uuid: string) {
        if (identityKeypair == null)
            return;

        await deleteFixedAuth("/api/service-entry/" + encodeURIComponent(uuid), identityKeypair);
        await refresh();
    }

    async function updateEntry(uuid: string, oldName: string, oldUsedService: string) {
        if (identityKeypair == null)
            return;

        const name = prompt("Enter a new name for the Service Entry", oldName);
        if (name == null)
            return;

        const usedService = prompt("Enter the new name or url of the Service. Can be blank", oldUsedService);
        if (usedService == null)
            return;

        const newEntry: ServiceEntryDto = {
            name: name,
            usedService: usedService,
            uuid: uuid
        }

        await putFixedAuth("/api/service-entry/" + encodeURIComponent(uuid), newEntry, identityKeypair);
        await refresh();
    }


    // TODO: Styling
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Service Entry Test</h2>

                <br/>
                <p>
                    Hello, {identityHandle}! (From {userHandle})<br/>
                    Quota: ({quotas?.currentServiceEntryCount} / {quotas?.maxServiceEntryCount})<br/>
                    These are your Service Entries:
                </p>

                <br/>
                <ul>
                    {serviceEntries.map((entry) => (<li key={entry.uuid}>
                            <span>{entry.name} - Service: {entry.usedService}</span> (UUID: {entry.uuid})
                            <span> </span>
                            <button onClick={() => {updateEntry(entry.uuid, entry.name, entry.usedService ?? "").then()}}>Update</button>
                            <span> </span>
                            <button onClick={() => {deleteEntry(entry.uuid).then()}}>Delete</button>
                        </li>
                    ))}
                </ul>
                <br/>

                <br/><hr/><br/>

                <button onClick={refresh}>Refresh</button><br/>
                <button onClick={createEntry}>Create new Entry</button><br/>

                <br/><hr/><br/>

                <div className={styles.MainButtons}>
                    <Link href="/user/identity-test">Identity Storage Test</Link>
                    <Link href="/user/home">Home</Link>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
