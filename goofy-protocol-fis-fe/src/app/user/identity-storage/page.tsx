'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {getAllUserIdentities, getBaseServerDomain, getIdentityKeypair, getKeypair} from "@/libs/auth-store";
import {useState} from "react";
import {deleteAuth, getAuth, postAuth} from "@/libs/req";
import {ExportIdentityKeypair, IdentityStorageEntryDto, MyIdentityEntryQuotasDto} from "@/libs/dtos";
import {
    asymmSignStr,
    deriveHandleFromPublicSplitKey,
    generateAsymmKeypair, secretSymmKeyFromFullKey,
    serializeFullKeypair,
    symmEncryptObj
} from "@/libs/crypto";
import {AsymmCryptoType, AsymmFullJsonKeypair} from "@/libs/crypto-types";
import {GlobalState, useGlobalState} from "@/libs/global-state";
import {downloadObjFile} from "@/libs/file-utils";

export default function Page() {
    const [identityEntries, setIdentityEntries] = useState<IdentityStorageEntryDto[]>([]);
    const [quotas, setQuotas] = useState<MyIdentityEntryQuotasDto | null>(null);

    useGlobalState(true, false, "NONE", async () => {
        await refresh();
    });

    async function refresh() {
        await getQuotas();
        await getIdentities();
    }

    async function getQuotas() {
        const quotas: MyIdentityEntryQuotasDto = await getAuth("/fis-api/identity-storage/quotas");
        setQuotas(quotas);
    }

    async function getIdentities() {
        setIdentityEntries(await getAllUserIdentities());
    }

    // TODO: Make it so that you can keep regenerating keypairs until you find one with a nice handle (like in the Register page), also ask the name first
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
            await postAuth("/fis-api/identity-storage", newEntry);
        } catch (e) {
            console.log(e);
            alert("Failed to create identity: " + (e as Error).message);
        }
        await refresh();
    }

    async function deleteIdentity(handle: string) {
        if (!confirm("Are you sure you want to delete identity " + handle + "? This cannot be undone."))
            return;

        await deleteAuth("/fis-api/identity-storage/" + encodeURIComponent(handle));
        await refresh();
    }

    async function exportLoginKeypair(entry: IdentityStorageEntryDto) {
        const kp = await getIdentityKeypair(entry.handle);
        const exportObj: ExportIdentityKeypair = {
            pub: kp.pub.serialize(),
            priv: kp.priv.serialize(),
            handleFull: entry.handle + "@" + await getBaseServerDomain()
        };
        downloadObjFile(exportObj, "identity-keypair-" + entry.name + "-" + entry.handle + ".json");
    }

    // TODO: Styling
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Identity Storage List</h2>

                <br/>
                <p>
                    Hello, {GlobalState.handle}!<br/>
                    Quota: ({quotas?.currentEntryCount} / {quotas?.maxEntryCount})<br/>
                    These are your Identities:
                </p>

                <br/>
                <ul>
                    {identityEntries.map((entry) => (<li key={entry.handle}>
                            <span>{entry.name} - {entry.handle}</span> (Size: {entry.pubSplitKey.length} / {entry.encKeypairEntry.length})
                            <span> &nbsp; </span>
                            <Link href={`/user/service-entry-list#${entry.handle}`}>Manage</Link>
                            <span> &nbsp; </span>
                            <button onClick={() => {exportLoginKeypair(entry).then()}}>Export Keypair</button>
                            <span> &nbsp; </span>
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
