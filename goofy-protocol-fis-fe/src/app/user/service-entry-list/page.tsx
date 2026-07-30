'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {getServiceEntries} from "@/libs/auth-store";
import {useState} from "react";
import {deleteFixedAuth, getFixedAuth, postFixedAuth, putFixedAuth} from "@/libs/req";
import {MyServiceEntryQuotasDto, ServiceEntryDto} from "@/libs/dtos";
import {GlobalState, useGlobalState} from "@/libs/global-state";

export default function Page() {
    const [serviceEntries, setServiceEntries] = useState<ServiceEntryDto[]>([]);
    const [quotas, setQuotas] = useState<MyServiceEntryQuotasDto | null>(null);

    useGlobalState(true, false, "IDENTITY", async () => {
        await refresh();
    });

    async function refresh() {
        await getQuotas();
        await getEntries();
    }

    async function getQuotas() {
        if (GlobalState.identityKeypair == null)
            return;

        const quotas: MyServiceEntryQuotasDto = await getFixedAuth("/api/service-entry/quotas", GlobalState.identityKeypair);
        setQuotas(quotas);
    }

    async function getEntries() {
        if (GlobalState.identityKeypair == null)
            return;

        setServiceEntries(await getServiceEntries(GlobalState.identityKeypair));
    }

    async function createEntry() {
        if (GlobalState.identityKeypair == null)
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
            await postFixedAuth("/api/service-entry", newEntry, GlobalState.identityKeypair);
        } catch (e) {
            console.log(e);
            alert("Failed to create Service Entry: " + (e as Error).message);
        }
        await refresh();
    }

    async function deleteEntry(uuid: string) {
        if (GlobalState.identityKeypair == null)
            return;

        await deleteFixedAuth("/api/service-entry/" + encodeURIComponent(uuid), GlobalState.identityKeypair);
        await refresh();
    }

    async function updateEntry(uuid: string, oldName: string, oldUsedService: string) {
        if (GlobalState.identityKeypair == null)
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

        await putFixedAuth("/api/service-entry/" + encodeURIComponent(uuid), newEntry, GlobalState.identityKeypair);
        await refresh();
    }

    // TODO: Styling
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Service Entry List</h2>

                <br/>
                <p>
                    Hello, {GlobalState.identityHandle}! (From {GlobalState.handle})<br/>
                    Quota: ({quotas?.currentServiceEntryCount} / {quotas?.maxServiceEntryCount})<br/>
                    These are your Service Entries:
                </p>

                <br/>
                <ul>
                    {serviceEntries.map((entry) => (<li key={entry.uuid}>
                            <span>{entry.name} - Service: {entry.usedService}</span> (UUID: {entry.uuid})
                            <span> </span>
                            <Link href={`/user/service-entry-manage#${GlobalState.identityHandle}@${entry.uuid}`}>Manage</Link>
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
                    <Link href="/user/identity-storage">Identity Storage</Link>
                    <Link href="/user/home">Home</Link>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
