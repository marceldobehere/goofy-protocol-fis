'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {getIdentityKeypair, getServiceEntry} from "@/libs/auth-store";
import {useEffect, useState} from "react";
import {goPath} from "@/libs/go-path";
import {isUser} from "@/libs/auth";
import {deleteFixedAuth, getFixedAuth, getFixedAuthBytes, postFixedAuth, putFixedAuth} from "@/libs/req";
import {
    ServiceBucketEntryDto,
    ServiceBucketPermissionDto,
    ServiceBucketQuotasDto,
    ServiceEntryDto
} from "@/libs/dtos";
import {AsymmFullKeyPair} from "@/libs/crypto-types";
import {readFileBytes, uploadData} from "@/libs/file-utils";

export default function Page() {
    const [identityKeypair, setIdentityKeypair] = useState<AsymmFullKeyPair | null>(null);
    const [identityHandle, setIdentityHandle] = useState<string | null>(null);
    const [serviceEntry, setServiceEntry] = useState<ServiceEntryDto | null>(null);

    const [perms, setPerms] = useState<ServiceBucketPermissionDto | null>(null);
    const [quotas, setQuotas] = useState<ServiceBucketQuotasDto | null>(null);
    const [entries, setEntries] = useState<ServiceBucketEntryDto[]>([]);


    useEffect(() => {(async () => {
        if (identityKeypair == null) {
            if (!(await isUser())) {
                goPath("/guest/login");
                return;
            }

            if (window.location.hash == "" || !window.location.hash.includes("@"))
                goPath("/user/home");

            const fragmentPart =  window.location.hash.slice(window.location.hash.lastIndexOf("#") + 1).split("@");
            // window.location.hash = "#" + fragmentHandle;
            const fragmentHandle = fragmentPart[0];
            const fragmentUuid = fragmentPart[1];

            try {
                const keypair = await getIdentityKeypair(fragmentHandle);
                const serviceEntry = await getServiceEntry(keypair, fragmentUuid);

                setIdentityHandle(fragmentHandle);
                setIdentityKeypair(keypair);
                setServiceEntry(serviceEntry);
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
        await getPerms();
        await getEntries();
    }

    async function getEntries() {
        if (identityKeypair == null || serviceEntry == null || identityHandle == null)
            return;

        const entries: ServiceBucketEntryDto[] = await getFixedAuth(`/api/service-bucket/${identityHandle}/${serviceEntry.uuid}/entry`, identityKeypair);
        entries.forEach(entry => {
            if (entry.createdAt != null)
                entry.createdAtDate = new Date(entry.createdAt)
        })

        entries.sort((a, b) => a.filename!.localeCompare(b.filename!) || 0);

        setEntries(entries);
    }

    async function getPerms() {
        if (identityKeypair == null || serviceEntry == null || identityHandle == null)
            return;

        const perms: ServiceBucketPermissionDto = await getFixedAuth(`/api/service-bucket/${serviceEntry.uuid}/perms`, identityKeypair);
        setPerms(perms);
    }

    async function getQuotas() {
        if (identityKeypair == null || serviceEntry == null || identityHandle == null)
            return;

        const quotas: ServiceBucketQuotasDto = await getFixedAuth(`/api/service-bucket/${identityHandle}/${serviceEntry.uuid}/quotas`, identityKeypair);
        setQuotas(quotas);
    }

    async function uploadEntry(fileUuid: string | null = null) {
        if (identityHandle == null || serviceEntry == null || identityKeypair == null)
            return;

        const data: File | null = await uploadData(false) as File;
        if (data == null)
            return;

        const filename = data.name;
        const dataType = data.type;
        const bytes = await readFileBytes(data);

        try {
            const uploadUrl = fileUuid == null ?
                `/api/service-bucket/${identityHandle}/${serviceEntry.uuid}/upload` :
                `/api/service-bucket/${identityHandle}/${serviceEntry.uuid}/upload/${fileUuid}`;
            await postFixedAuth(uploadUrl,
                bytes, identityKeypair, new Map([["Content-Type", dataType], ["X-Filename", encodeURIComponent(filename)], /*["X-Cache-Duration", "NONE"]*/]));
        } catch (e) {
            console.log(e);
            alert("Failed to upload Bucket Entry: " + (e as Error).message);
        }
        await refresh();
    }

    async function deleteEntry(fileUuid: string) {
        if (identityHandle == null || serviceEntry == null || identityKeypair == null)
            return;

        try {
            await deleteFixedAuth(`/api/service-bucket/${identityHandle}/${serviceEntry.uuid}/entry/${fileUuid}`, identityKeypair);
        } catch (e) {
            console.log(e);
            alert("Failed to delete Bucket Entry: " + (e as Error).message);
        }
        await refresh();
    }

    async function viewEntry(fileUuid: string) {
        if (identityHandle == null || serviceEntry == null || identityKeypair == null)
            return;

        try {
            // Load Data
            const details: ServiceBucketEntryDto = await getFixedAuth(`/api/service-bucket/${identityHandle}/${serviceEntry.uuid}/entry/${fileUuid}`, identityKeypair);
            const data: Uint8Array = await getFixedAuthBytes(`/api/service-bucket/${identityHandle}/${serviceEntry.uuid}/content/${fileUuid}`, identityKeypair);

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
        if (identityHandle == null || serviceEntry == null || identityKeypair == null)
            return;

        const details: ServiceBucketEntryDto = await getFixedAuth(`/api/service-bucket/${identityHandle}/${serviceEntry.uuid}/entry/${fileUuid}`, identityKeypair);
        alert(`Details for Bucket Entry ${fileUuid}:\n` + JSON.stringify(details));
    }

    async function bucketChangePerm(handle: string | null, insert: boolean, read: boolean) {
        if (perms == null || identityHandle == null || serviceEntry == null || identityKeypair == null)
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
            await putFixedAuth(`/api/service-bucket/${serviceEntry.uuid}/perms`, localPerms, identityKeypair);
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
                <h2 className={styles.Title}>Manage Service Entry</h2>

                <br/>
                <p>
                    Checking Service Entry &quot;{serviceEntry?.name || serviceEntry?.usedService || serviceEntry?.uuid}&quot; (for {identityHandle}) <br/>
                    Service Entry List Quota: (Count: {quotas?.currentItemCount} / {quotas?.maxItemCount}, Size: {((quotas?.currentBucketSize ?? 0) / (1000*1000)).toFixed(2)}MB / {((quotas?.maxBucketSize ?? 0) / (1000*1000)).toFixed(2)}MB) (Max Item Size: {((quotas?.maxItemSize ?? 0) / (1000*1000)).toFixed(2)}MB)<br/>
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
                        <span>{entry.filename} ({entry.fileUuid?.substring(0, 16)}...) (Type: {entry.contentType}, Size: {((entry.contentSize ?? 0) / (1000*1000)).toFixed(2)}MB, Created At: {entry.createdAtDate!.toLocaleDateString()})</span>
                        <span> </span>
                        <button onClick={() => {getEntryDetails(entry.fileUuid).then()}}>Details</button>
                        <span> </span>
                        <button onClick={() => {viewEntry(entry.fileUuid).then()}}>View</button>
                        <span> </span>
                        <button onClick={() => {uploadEntry(entry.fileUuid).then()}}>Reupload</button>
                        <span> </span>
                        <button onClick={() => {deleteEntry(entry.fileUuid).then()}}>Delete</button>
                    </li>))}
                </ul>

                <br/>
                <br/><hr/><br/>

                <button onClick={() => {uploadEntry().then()}}>Upload Entry</button><br/>
                <button onClick={refresh}>Refresh</button><br/>

                <br/><hr/><br/>

                <div className={styles.MainButtons}>
                    <Link href={`/user/service-entry-list#${identityHandle}`}>Service Entry List</Link>
                    <Link href="/user/identity-storage">Identity Storage</Link>
                    <Link href="/user/home">Home</Link>
                    <Link href={"/"}>Index</Link>
                </div>
            </div>
        </main>
    );
}
