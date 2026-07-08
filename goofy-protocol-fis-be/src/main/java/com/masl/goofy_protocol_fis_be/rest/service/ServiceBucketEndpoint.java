package com.masl.goofy_protocol_fis_be.rest.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-bucket")
@Tag(name = "Service Bucket Access", description = "Endpoints related to accessing Buckets.")
public class ServiceBucketEndpoint {
    /*
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
}
