'use client';

import styles from "./page.module.css";
import Link from "next/link";
import {useState} from "react";
import {deleteFixedAuth, getFixedAuth, postFixedAuth} from "@/libs/req";
import {ServiceDbQuotasDto, ServiceTableEntryDto} from "@/libs/dtos";
import {GlobalState, useGlobalState} from "@/libs/global-state";
import TableView from "@/app/user/service-entry-manage-table/table-view/comp";

export default function Page() {
    const [quotas, setQuotas] = useState<ServiceDbQuotasDto | null>(null);
    const [entries, setEntries] = useState<ServiceTableEntryDto[]>([]);
    const [currTableUuid, setCurrTableUuid] = useState<string | null>(null);

    useGlobalState(true, false, "IDENTITY@SERVICE", async () => {
        await refresh();
    });

    async function refresh() {
        await getQuotas();
        await getEntries();
    }

    async function getEntries() {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const entries: ServiceTableEntryDto[] = await getFixedAuth(`/fis-api/service-table/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry`, GlobalState.identityKeypair);
        entries.sort((a, b) => a.tableName!.localeCompare(b.tableName!) || 0);

        setEntries(entries);
    }

    async function getQuotas() {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const quotas: ServiceDbQuotasDto = await getFixedAuth(`/fis-api/service-table/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/quotas`, GlobalState.identityKeypair);
        setQuotas(quotas);
    }

    async function createTableEntry() {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const inputJson = prompt("Enter DTO String");
        if (inputJson == null || inputJson == "")
            return;

        try {
            const res: ServiceTableEntryDto = await postFixedAuth(`/fis-api/service-table/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry`, JSON.parse(inputJson), GlobalState.identityKeypair);
            console.log(res);
            alert("Created Table Entry: " + JSON.stringify(res, null, 2));
            await refresh();
        } catch (e) {
            console.error(e);
            alert("Failed to create Table Entry: " + (e as Error).message);
        }
    }

    async function deleteTableEntry(tableUuid: string) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        if (!confirm("Are you sure you want to delete the Table Entry?"))
            return;

        await deleteFixedAuth(`/fis-api/service-table/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${tableUuid}`, GlobalState.identityKeypair);
        if (tableUuid == currTableUuid)
            setCurrTableUuid(null);
        await refresh();
    }

    // TODO: Styling
    // TODO: Add Locking
    // TODO: Improve Add Rows, Add Table, Query Builder, Update Schema, Update Permissions, etc.
    return (
        <main>
            <div className={styles.MainCont}>
                <h2 className={styles.Title}>Manage Service Entry (DB /Tables)</h2>

                <br/>
                <p>
                    Checking Service Entry &quot;{GlobalState.serviceEntry?.name || GlobalState.serviceEntry?.usedService || GlobalState.serviceEntry?.uuid}&quot; (for {GlobalState.identityHandle}) <br/>
                    DB Quotas: (Count: {quotas?.currTableCount} / {quotas?.maxTableCount}, Size: {((quotas?.currDbSize ?? 0) / (1000*1000)).toFixed(2)}MB / {((quotas?.maxDbSize ?? 0) / (1000*1000)).toFixed(2)}MB) (Max Field Size: {((quotas?.maxFieldSize ?? 0) / (1000*1000)).toFixed(2)}MB)<br/>
                    Here is the information for your Service Entry:
                </p>

                <br/><hr/><br/>
                <h3>Tables</h3>

                <br/>
                <ul>
                    {entries?.map((entry) => (<li key={entry.tableUuid}>
                        <span>{entry.tableUuid} ({entry.tableUuid?.substring(0, 16)}...) ({entry.columns?.length} Columns, Schema Version: {entry.schemaVersion})</span><br/>
                        <span> </span>
                        <button onClick={() => {setCurrTableUuid(entry.tableUuid!)}}>View</button>
                        <span> </span>
                        <button onClick={() => {deleteTableEntry(entry.tableUuid!).then()}}>Delete</button>
                        <br/>&nbsp;
                    </li>))}
                </ul>


                <br/><br/>
                <button onClick={createTableEntry}>Create Table</button><br/>
                <button onClick={() => {setCurrTableUuid(null)}}>Reset View</button>


                <br/><hr/><br/>
                <h3>Table View</h3>
                <br/>
                {currTableUuid == null ? (<></>) : <TableView tableUuid={currTableUuid}></TableView>}
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
