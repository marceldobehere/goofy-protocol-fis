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

            setMsg(`Loading Bucket Entry: ${details.filename} (${details.contentType}, ${data.byteLength} bytes)...`);

            // Create Blob URL
            const blob = new Blob([data as BlobPart], { type: details.contentType });
            const url = URL.createObjectURL(blob);

            // Open Window
            window.open(url, "_self");
        } catch (e) {
            const derivedHandle = await deriveHandleFromPublicSplitKey(currKeypair.pub);
            setMsg(`Failed to fetch Bucket Entry: ${fileUuid} - You (${derivedHandle}) might not have permission to access this entry.`);
            alert("Failed to fetch Bucket Entry: " + (e as Error).message);
        }
    });

    return (
        <main>
            <p>{msg}</p>
        </main>
    );
}
