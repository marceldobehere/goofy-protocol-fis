package com.masl.goofy_protocol_fis_be.rest.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-bucket")
@Tag(name = "Service Bucket Access", description = "Endpoints related to accessing Buckets.")
public class ServiceBucketEndpoint {
    /*
    // TODO: Make sure that when using ServiceEntry.uuid the OUTSIDE_ENTITIES also have the handle of the user in the request, so that it can be used if the user moved FIS and the server ooesn't know yet?


    ### Service Bucket Access
    These Endpoints get used by services to access the buckets.

    #### Get Buckets

    #### Get Buckets Quota
    How much storage buckets can use and have used and how many buckets can be created/have been created

    #### Get Bucket
    How many entries, how much storage used, etc.
    Is the Bucket publically visible.

    #### Create Bucket
    Define Visibility

    #### Set Bucket Visibility
    Basically is the Bucket only visible by handles with access or to everyone (private service data or public social media post entries)

    #### Delete Bucket



    #### Get Entries for Bucket

    #### Add Entry to Bucket
    Upload File/Data to Bucket alongside metadata.
    TODO: Decide if it should be a POST with raw data + custom JSON metadata header or a multipart form with metadata and file seperate

    #### Get Entry Info from Bucket
    Get Info about the entry but not the data itself.
    Can be if it exists, size, maybe timestamp, filename with extension?

    #### Get Entry from Bucket
    Get Raw Data from an Entry

    #### Delete Entry from Bucket
    */

        /*
    // TODO: This shoul be inside the ServiceBucket and ServiceTable Endpoints
    ### Service Data Access Configuration
    Being able to list, view and modify the tables / data inside.

    How much storage can be used / is used, and how many tables can be created / have been created. Also how many columns/rows a table can have.

    Also being able to set the visibility of data (either private or public or select services maybe?)


    The finding Tables/Buckets + direct manipulation of Table/Bucket data + Visibility Status can use the API inside the `Service Table Access` Section by using the global scope and correct Service name.

    #### Get Total Quota Stats
    Aggregation of Table and Bucket Stats

    #### Get Table Access

    #### Set Table Access
    Manage which handles can access the table

    #### Get Bucket Access

    #### Set Bucket Access
    Manage which handles can access the table and if its read/write or readonly
    */
}
