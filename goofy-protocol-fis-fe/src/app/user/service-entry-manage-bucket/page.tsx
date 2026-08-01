'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {useState} from "react";
import {deleteFixedAuth, getFixedAuth, getFixedAuthBytes, postFixedAuth, putFixedAuth} from "@/libs/req";
import {ServiceBucketEntryDto, ServiceBucketPermissionDto, ServiceBucketQuotasDto} from "@/libs/dtos";
import {readFileBytes, uploadData} from "@/libs/file-utils";
import {GlobalState, useGlobalState} from "@/libs/global-state";
import {getBaseServerUrl} from "@/libs/auth-store";

export default function Page() {
    const [perms, setPerms] = useState<ServiceBucketPermissionDto | null>(null);
    const [quotas, setQuotas] = useState<ServiceBucketQuotasDto | null>(null);
    const [entries, setEntries] = useState<ServiceBucketEntryDto[]>([]);

    useGlobalState(true, false, "IDENTITY@SERVICE", async () => {
        await refresh();
    });

    async function refresh() {
        await getQuotas();
        await getPerms();
        await getEntries();
    }

    async function getEntries() {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const entries: ServiceBucketEntryDto[] = await getFixedAuth(`/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry`, GlobalState.identityKeypair);
        entries.forEach(entry => {
            if (entry.createdAt != null)
                entry.createdAtDate = new Date(entry.createdAt)
        })

        entries.sort((a, b) => a.filename!.localeCompare(b.filename!) || 0);

        setEntries(entries);
    }

    async function getPerms() {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const perms: ServiceBucketPermissionDto = await getFixedAuth(`/fis-api/service-bucket/${GlobalState.serviceEntry.uuid}/perms`, GlobalState.identityKeypair);
        setPerms(perms);
    }

    async function getQuotas() {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const quotas: ServiceBucketQuotasDto = await getFixedAuth(`/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/quotas`, GlobalState.identityKeypair);
        setQuotas(quotas);
    }

    async function uploadEntry(fileUuid: string | null = null) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const data: File | null = await uploadData(false) as File;
        if (data == null)
            return;

        const filename = data.name;
        const dataType = data.type;
        const bytes = await readFileBytes(data);

        try {
            const uploadUrl = fileUuid == null ?
                `/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/upload` :
                `/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/upload/${fileUuid}`;
            await postFixedAuth(uploadUrl,
                bytes, GlobalState.identityKeypair, new Map([["Content-Type", dataType], ["X-Filename", encodeURIComponent(filename)], /*["X-Cache-Duration", "NONE"]*/]));
        } catch (e) {
            console.log(e);
            alert("Failed to upload Bucket Entry: " + (e as Error).message);
        }
        await refresh();
    }

    async function deleteEntry(fileUuid: string) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        if (!confirm("Are you sure you want to delete the Service Entry?"))
            return;

        try {
            await deleteFixedAuth(`/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${fileUuid}`, GlobalState.identityKeypair);
        } catch (e) {
            console.log(e);
            alert("Failed to delete Bucket Entry: " + (e as Error).message);
        }
        await refresh();
    }

    async function setEntryPerms(fileUuid: string, read: boolean) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const details: ServiceBucketEntryDto = await getFixedAuth(`/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${fileUuid}`, GlobalState.identityKeypair);

        if (read) {
            const newReadAccess = prompt(`Set the new read access as a list of handles separated by a space. (* for everyone)`, details.handlesWithReadPerms.join(" "));
            if (newReadAccess == null)
                return;

            details.handlesWithReadPerms = newReadAccess.split(" ");
        } else {
            const newWriteAccess = prompt(`Set the new write access as a list of handles separated by a space.`, details.handlesWithWritePerms.join(" "));
            if (newWriteAccess == null)
                return;

            details.handlesWithWritePerms = newWriteAccess.split(" ");
        }

        try {
            await putFixedAuth(`/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${fileUuid}`, details, GlobalState.identityKeypair);
        } catch (e) {
            console.log(e);
            alert("Failed to upload Bucket Entry: " + (e as Error).message);
        }
    }

    async function shareEntry(fileUuid: string) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const backend = await getBaseServerUrl();
        // OLD
        // // Base Frontend URL + /guest/view + ?tempBackendUrl= + urlencode(currentBackend) + # + identityHandle + @ + serviceEntryUuid + @ + fileUuid
        // const shareUrl = `${basePath}/guest/view?tempBackendUrl=${encodeURIComponent(backend)}#${GlobalState.identityHandle}@${GlobalState.serviceEntry.uuid}@${fileUuid}`;

        // Backend URL + /short/bucket/{idHandle}/{serviceUuid}/{fileUuid}
        const shareUrl = `${backend}/short/bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/${fileUuid}`;

        // Copy to clipboard
        await navigator.clipboard.writeText(shareUrl);
        alert(`Share URL copied to clipboard:\n${shareUrl}`);
    }

    async function viewEntry(fileUuid: string) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        try {
            // Load Data
            const details: ServiceBucketEntryDto = await getFixedAuth(`/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${fileUuid}`, GlobalState.identityKeypair);
            const data: Uint8Array = await getFixedAuthBytes(`/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/content/${fileUuid}`, GlobalState.identityKeypair);

            // Create Blob URL
            const blob = new Blob([data as BlobPart], { type: details.contentType });
            const url = URL.createObjectURL(blob);

            // Open Window
            window.open(url, "_blank");

            // Cleanup
            setTimeout(() => URL.revokeObjectURL(url), 60_000);
        } catch (e) {
            console.log(e);
            alert("Failed to fetch Bucket Entry: " + (e as Error).message);
        }
        await refresh();
    }

    async function getEntryDetails(fileUuid: string) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const details: ServiceBucketEntryDto = await getFixedAuth(`/fis-api/service-bucket/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${fileUuid}`, GlobalState.identityKeypair);
        alert(`Details for Bucket Entry ${fileUuid}:\n` + JSON.stringify(details));
    }

    async function bucketChangePerm(handle: string | null, insert: boolean, read: boolean) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null || perms == null)
            return;

        if (handle == null)
            handle = prompt("Enter Handle");
        if (handle == null)
            return;

        const localPerms = JSON.parse(JSON.stringify(perms)) as ServiceBucketPermissionDto;

        if (read) {
            if (insert)
                localPerms.handlesWithReadPerms.push(handle);
            else
                localPerms.handlesWithReadPerms = localPerms.handlesWithReadPerms.filter(h => h != handle);
        } else {
            if (insert)
                localPerms.handlesWithWritePerms.push(handle);
            else
                localPerms.handlesWithWritePerms = localPerms.handlesWithWritePerms.filter(h => h != handle);
        }

        try {
            await putFixedAuth(`/fis-api/service-bucket/${GlobalState.serviceEntry.uuid}/perms`, localPerms, GlobalState.identityKeypair);
        } catch (e) {
            console.log(e);
            alert("Failed to edit Bucket Entry Permissions: " + (e as Error).message);
        }
        await refresh();
    }


    // TODO: Edit Permissions + Filename + Cache Duration of an Entry
    // TODO: Test Accessing Data from other places with Permissions
    // TODO: Test Updating Bucket Entry Metadata + Permissions
    // TODO: Styling
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Manage Service Entry (Bucket)</h2>

                <br/>
                <p>
                    Checking Service Entry &quot;{GlobalState.serviceEntry?.name || GlobalState.serviceEntry?.usedService || GlobalState.serviceEntry?.uuid}&quot; (for {GlobalState.identityHandle}) <br/>
                    Bucket Quotas: (Count: {quotas?.currentItemCount} / {quotas?.maxItemCount}, Size: {((quotas?.currentBucketSize ?? 0) / (1000*1000)).toFixed(2)}MB / {((quotas?.maxBucketSize ?? 0) / (1000*1000)).toFixed(2)}MB) (Max Item Size: {((quotas?.maxItemSize ?? 0) / (1000*1000)).toFixed(2)}MB)<br/>
                    Here is the information for your Service Entry:
                </p>

                <br/><hr/><br/>
                <h3>Permissions</h3>

                <br/>
                <p>Bucket Read Access:</p><br/>
                <ul>
                    {perms?.handlesWithReadPerms.map((handle => (<li key={handle}>
                        {handle}
                        <span> </span>
                        <button onClick={() => {bucketChangePerm(handle, false, true).then()}}>Remove</button>
                    </li>)))}
                </ul>
                <br/>
                <button onClick={() => {bucketChangePerm(null, true, true).then()}}>Add Read Perm</button>

                <br/><br/><br/>
                <p>Bucket Write Access:</p><br/>
                <ul>
                    {perms?.handlesWithWritePerms.map((handle => (<li key={handle}>
                        {handle}
                        <span> </span>
                        <button onClick={() => {bucketChangePerm(handle, false, false).then()}}>Remove</button>
                    </li>)))}
                </ul>
                <br/>
                <button onClick={() => {bucketChangePerm(null, true, false).then()}}>Add Write Perm</button>

                <br/>
                <br/><hr/><br/>
                <h3>Entries</h3>

                <br/>
                <ul>
                    {entries?.map((entry) => (<li key={entry.fileUuid}>
                        <span>{entry.filename} ({entry.fileUuid?.substring(0, 16)}...) (Type: {entry.contentType}, Size: {((entry.contentSize ?? 0) / (1000*1000)).toFixed(2)}MB, Created At: {entry.createdAtDate!.toLocaleDateString()})</span><br/>
                        <span> </span>
                        <button onClick={() => {getEntryDetails(entry.fileUuid).then()}}>Details</button>
                        <span> </span>
                        <button onClick={() => {viewEntry(entry.fileUuid).then()}}>View</button>
                        <span> </span>
                        <button onClick={() => {uploadEntry(entry.fileUuid).then()}}>Reupload</button>
                        <span> </span>
                        <button onClick={() => {deleteEntry(entry.fileUuid).then()}}>Delete</button>
                        <span> </span>
                        <button onClick={() => {shareEntry(entry.fileUuid).then()}}>Share Link</button>
                        <span> </span>
                        <button onClick={() => {setEntryPerms(entry.fileUuid, true).then()}}>Set Read Perms</button>
                        <br/>&nbsp;
                    </li>))}
                </ul>

                <br/>
                <br/><hr/><br/>

                <button onClick={() => {uploadEntry().then()}}>Upload Entry</button><br/>
                <button onClick={refresh}>Refresh</button><br/>

                <br/><hr/><br/>

                <div className={styles.MainButtons}>
                    <Link href={`/user/service-entry-list#${GlobalState.identityHandle}`}>Service Entry List</Link>
                    <Link href="/user/identity-storage">Identity Storage</Link>
                    <Link href="/user/home">Home</Link>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
