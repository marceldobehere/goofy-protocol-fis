'use client';

import {useGlobalState} from "@/libs/global-state";
import {goPath} from "@/libs/go-path";
import {getKeypair, hasKeypair} from "@/libs/auth-store";
import {deriveHandleFromPublicSplitKey, generateAsymmKeypair} from "@/libs/crypto";
import {ServiceBucketEntryDto} from "@/libs/dtos";
import {getFixedAuth, getFixedAuthBytes} from "@/libs/req";
import {useState} from "react";

export default function Page() {
    const [msg, setMsg] = useState("Trying to fetch data...");
    const [blob, setBlob] = useState<Blob | null>(null);

    useGlobalState(false, false, "NONE", async () => {
        const fragmentParts =  window.location.hash.slice(window.location.hash.lastIndexOf("#") + 1).split("@");
        console.debug("Fragment Parts: ", fragmentParts);
        if (fragmentParts.length != 3) {
            alert("Invalid fragment format. Expected format: handle@serviceUuid@fileUuid");
            goPath("/");
            return;
        }

        const handle = fragmentParts[0];
        const serviceUuid = fragmentParts[1];
        const fileUuid = fragmentParts[2];

        // Use current keypair or create a new temporary one
        const currKeypair = await hasKeypair() ? await getKeypair() : await generateAsymmKeypair();
        console.debug("Using Keypair: ", currKeypair);

        setMsg(`Fetching Bucket Entry for handle: ${handle}, serviceUuid: ${serviceUuid}, fileUuid: ${fileUuid}...`);

        try {
            // Load Data
            const details: ServiceBucketEntryDto = await getFixedAuth(`/api/service-bucket/${handle}/${serviceUuid}/entry/${fileUuid}`, currKeypair);
            const data: Uint8Array = await getFixedAuthBytes(`/api/service-bucket/${handle}/${serviceUuid}/content/${fileUuid}`, currKeypair);

            setMsg(`Loading Bucket Entry: ${details.filename}\" (${details.contentType}, ${Math.round(100 * (data.byteLength / (1024*1024))) / 100} MB)...`);

            // Create Blob URL
            const blob = new Blob([data as BlobPart], { type: details.contentType });
            setBlob(blob);
            await view(blob);
        } catch (e) {
            const derivedHandle = await deriveHandleFromPublicSplitKey(currKeypair.pub);
            setMsg(`Failed to fetch Bucket Entry: ${fileUuid} - You (${derivedHandle}) might not have permission to access this entry.`);
            alert("Failed to fetch Bucket Entry: " + (e as Error).message);
        }
    });

    async function view(tBlob: Blob | null = null) {
        if (blob == null && tBlob == null)
            return;
        const url = URL.createObjectURL(blob ?? tBlob as Blob);

        // Open Window
        window.open(url, "_self");
    }

    return (
        <main>
            <p>{msg}</p><br/>
            {blob == null ? (<></>) : <button onClick={() => {view().then()}}>View</button>}
        </main>
    );
}
