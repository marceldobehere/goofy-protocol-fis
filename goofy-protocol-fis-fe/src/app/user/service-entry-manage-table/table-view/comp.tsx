'use client';

import styles from "./comp.module.css";
import {useState} from "react";
import {
    ServiceBucketEntryDto,
    ServiceTableEntryDto,
    ServiceTableQueryResultDto, TableBasicQueryDto,
    TableColumnDto,
    TableSelectDto
} from "@/libs/dtos";
import {deleteBodyFixedAuth, getFixedAuth, postFixedAuth} from "@/libs/req";
import {GlobalState, useAsyncEffect} from "@/libs/global-state";
import {renderColToString, renderQuery} from "@/app/user/service-entry-manage-table/table-view/helper";

export default function TableView({tableUuid, extraQuery = undefined}: {tableUuid: string, extraQuery?: TableBasicQueryDto}) {
    const [currQuery, setCurrQuery] = useState<TableBasicQueryDto | null>(extraQuery ?? null);
    const [tableName, setTableName] = useState<string>("");
    const [cols, setCols] = useState<TableColumnDto[]>([]);
    const [rows, setRows] = useState<unknown[][]>([]);
    const [hasMoreRows, setHasMoreRows] = useState<boolean>(false);

    useAsyncEffect(async () => {
        await refresh();
    }, [tableUuid, currQuery]);

    async function refresh(append: boolean = false) {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        try {
            const entry: ServiceTableEntryDto = await getFixedAuth(`/api/service-table/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${tableUuid}`, GlobalState.identityKeypair);

            const oldRows = rows;

            // Get Rows
            const colNames = entry.columns!.map(c => c.colName);
            const query: TableSelectDto = {colNames};
            if (currQuery == null) {
                query.basicQuery = {offset: append ? oldRows.length : null};
            } else {
                query.basicQuery = currQuery;
                if (currQuery.offset == null)
                    query.basicQuery.offset = append ? oldRows.length : null;
            }

            const rowData: ServiceTableQueryResultDto = await postFixedAuth(`/api/service-table/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${tableUuid}/query`, query, GlobalState.identityKeypair);

            // Update Display
            setTableName(entry.tableName!);
            setRows([]);
            setCols(entry.columns!);
            if (append)
                setRows([...oldRows, ...rowData.rows]);
            else
                setRows(rowData.rows);
            setHasMoreRows(rowData.resultTruncated);
        } catch (e) {
            console.error("Failed loading table View", e);
        }
    }

    async function insertRow() {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const inputJson = prompt("Enter DTO String: " + cols.map(c => c.colName).join(", "));
        if (inputJson == null || inputJson == "")
            return;

        try {
            const res: ServiceBucketEntryDto = await postFixedAuth(`/api/service-table/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${tableUuid}/rows`, JSON.parse(inputJson), GlobalState.identityKeypair);
            console.log(res);
            await refresh();
        } catch (e) {
            console.error(e);
            alert("Failed to insert Row: " + (e as Error).message);
        }
    }

    async function doQuery() {
        const inputJson = prompt("Enter Query DTO String: ");
        if (inputJson == null)
            return;

        if (inputJson == "")
            setCurrQuery(null);
        else
            setCurrQuery(JSON.parse(inputJson));
    }

    async function deleteQuery() {
        if (GlobalState.identityHandle == null || GlobalState.identityKeypair == null || GlobalState.serviceEntry == null)
            return;

        const inputJson = prompt("Enter Query DTO String: ");
        if (inputJson == null || inputJson == "")
            return;

        if (!confirm("Are you sure you want to delete rows matching the query? This cannot be undone."))
            return;

        try {
            const res: ServiceBucketEntryDto = await deleteBodyFixedAuth(`/api/service-table/${GlobalState.identityHandle}/${GlobalState.serviceEntry.uuid}/entry/${tableUuid}/rows`, JSON.parse(inputJson), GlobalState.identityKeypair);
            console.log(res);
            await refresh();
        } catch (e) {
            console.error(e);
            alert("Failed to insert Row: " + (e as Error).message);
        }
    }

    return (<div>
        <div className={styles.TableWrap}>
            <table className={styles.Table} border={1}>
                <caption>
                    Table View for: {tableName}
                    {currQuery == null ? (<></>) : (<><br/>Query: {renderQuery(currQuery)}</>)}
                </caption>
                <thead>
                <tr>{cols.map((col => (<th key={col.colName} title={`${col.colName} (${col.type} (${col.typeSize})) - [${col.constraints}] - Default: ${col.defaultValue}`}>{col.colName} ({col.constraints.map((s) => s[0]).join("/")})</th>)))}</tr>
                </thead>
                <tbody>
                {rows.map((row, index) => (
                    <tr key={index}>
                        {row.map(((cell, index) => (<td key={index}>{renderColToString(cell, cols[index])}</td>)))}
                    </tr>))}
                {hasMoreRows ? <tr><td colSpan={cols.length} className={styles.LoadMore}><button onClick={() => {refresh(true).then()}}>Load More</button></td></tr> : (<></>)}
                </tbody>
            </table>
        </div>
        <br/>

        <button onClick={() => {refresh(false).then()}}>Refresh</button>
        <span> &nbsp; </span>
        <button onClick={insertRow}>Insert Row</button>
        <span> &nbsp; </span>
        <button onClick={doQuery}>Query</button>
        <span> &nbsp; </span>
        <button onClick={deleteQuery}>Delete Query</button>
    </div>);
}