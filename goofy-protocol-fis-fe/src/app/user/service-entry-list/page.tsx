'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {getServiceEntries} from "@/libs/auth-store";
import {useState} from "react";
import {deleteFixedAuth, getAuth, getFixedAuth, postFixedAuth, putAuth, putFixedAuth} from "@/libs/req";
import {IdentityPublicData, MyServiceEntryQuotasDto, ServiceEntryDto, ServicePublicDataUpdate} from "@/libs/dtos";
import {GlobalState, useGlobalState} from "@/libs/global-state";

export default function Page() {
    const [serviceEntries, setServiceEntries] = useState<ServiceEntryDto[]>([]);
    const [quotas, setQuotas] = useState<MyServiceEntryQuotasDto | null>(null);

    useGlobalState(true, false, "IDENTITY", async () => {
        await refresh();

        // Example: ?setPublicServiceEntry={"serverName": "testo123", "newData": {"value": "yes", "number": 12}}
        const queryParams = new URLSearchParams(window.location.search);
        const setPublicServiceEntryStr = queryParams.get("setPublicServiceEntry");
        if (setPublicServiceEntryStr != null) {
            const updateData: ServicePublicDataUpdate = JSON.parse(setPublicServiceEntryStr);
            console.log(updateData);
            await setPublicServiceEntry(updateData.serverName, JSON.parse(updateData.newData));

            // Remove from URL
            const newUrl = new URL(window.location.href);
            newUrl.searchParams.delete("setPublicServiceEntry");
            window.location.search = newUrl.search;
        }
    });

    async function refresh() {
        await getQuotas();
        await getEntries();
    }

    async function getQuotas() {
        if (GlobalState.identityKeypair == null)
            return;

        const quotas: MyServiceEntryQuotasDto = await getFixedAuth("/fis-api/service-entry/quotas", GlobalState.identityKeypair);
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
            await postFixedAuth("/fis-api/service-entry", newEntry, GlobalState.identityKeypair);
        } catch (e) {
            console.log(e);
            alert("Failed to create Service Entry: " + (e as Error).message);
        }
        await refresh();
    }

    async function deleteEntry(uuid: string) {
        if (GlobalState.identityKeypair == null)
            return;

        if (!confirm("Are you sure you want to delete the Service Entry?"))
            return;

        await deleteFixedAuth("/fis-api/service-entry/" + encodeURIComponent(uuid), GlobalState.identityKeypair);
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

        await putFixedAuth("/fis-api/service-entry/" + encodeURIComponent(uuid), newEntry, GlobalState.identityKeypair);
        await refresh();
    }

    async function viewPublicData() {
        const data: IdentityPublicData = await getAuth(`/fis-api/identity-storage/public/${GlobalState.identityHandle}`);
        alert("Public Identity Data:\n" + JSON.stringify(data, null, 4));
    }

    async function setPublicData() {
        const data: IdentityPublicData = await getAuth(`/fis-api/identity-storage/public/${GlobalState.identityHandle}`);
        alert("Public Identity Data:\n" + JSON.stringify(data, null, 4));

        const newDataStr = prompt("Enter new Public Identity Data as JSON", JSON.stringify(data));
        if (newDataStr == null || newDataStr == "")
            return;

        try {
            await putAuth(`/fis-api/identity-storage/public/${GlobalState.identityHandle}`, JSON.parse(newDataStr));
            alert("Public Identity Data updated successfully");
        } catch (e) {
            console.log(e);
            alert("Failed to update Public Identity Data: " + (e as Error).message);
        }
    }

    async function setPublicServiceEntry(serviceName: string, newData: object) {
        const data: IdentityPublicData = await getAuth(`/fis-api/identity-storage/public/${GlobalState.identityHandle}`);
        if (!confirm(`A Service wants to set/update the Public Data Entry for Service \"${serviceName}\" from:\n${JSON.stringify(data.services[serviceName] ?? null)}\n to:\n${JSON.stringify(newData)}\n\nDo you want to allow this?`))
            return;

        data.services[serviceName] = newData as never;
        try {
            await putAuth(`/fis-api/identity-storage/public/${GlobalState.identityHandle}`, data);
            alert("Public Identity Data updated successfully");
        } catch (e) {
            console.log(e);
            alert("Failed to update Public Identity Data: " + (e as Error).message);
        }
    }

    // TODO: Add Button here to download/export/copy the keypair to use for logging in to / registering for services.
    // TODO: Styling
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Identity Storage Management - Service Entry List</h2>

                <br/>
                <p>
                    Hello, {GlobalState.identityHandle}! (From {GlobalState.handle})<br/>
                    <button onClick={viewPublicData}>View Public Identity Data</button><span> &nbsp; </span>
                    <button onClick={setPublicData}>Set Public Identity Data</button>

                    <br/><br/>
                    Quota: ({quotas?.currentServiceEntryCount} / {quotas?.maxServiceEntryCount})<br/>
                    These are your Service Entries:
                </p>

                <br/>
                <ul>
                    {serviceEntries.map((entry) => (<li key={entry.uuid}>
                            <span>{entry.name} - Service: {entry.usedService}</span> (UUID: {entry.uuid})
                            <span> </span>
                            <Link href={`/user/service-entry-manage-bucket#${GlobalState.identityHandle}@${entry.uuid}`}>Manage Bucket</Link>
                            <span> </span>
                            <Link href={`/user/service-entry-manage-table#${GlobalState.identityHandle}@${entry.uuid}`}>Manage Tables</Link>
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
