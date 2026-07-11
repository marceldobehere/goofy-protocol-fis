package com.masl.goofy_protocol_fis_be.rest.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-entry-config")
@Tag(name = "Service Entry Config", description = "Endpoints related to configuring Service Entries. <br>For Each Identity Users may create \"Service Entries\" which the User can then use to store data for the service & identity. <br>Additionally the User can allow a Service to read/write some tables")
public class ServiceEntryConfigEndpoint {
    // For Each Identity you may create "Service Entries" which a Service can use to store data for the identity.
    // Each Entry basically has a unique name and has a defined Storage Limit/Quota.
    // The Entries also have access defined for which handles can read/write to them. (Default always has at least the User and the Identity)
    // The Entries consist of a DB with Tables and Bucket for binary Data

    // TODO: Look into if I should maybe split it into 2 DBs per service entry: 1 for the service to access and one for the user to access (for example useful if the user stores extra info like chat messages which the server shouldnt interfere with / know about)

    /*
    ### Service Entry Configuration
    Endpoints related to configuring Service Data Access Entries.

    A service can request to store data (in tables or data buckets) for a handle / user identity. This needs to be managed and configured. So users need to be able to create Entries for services with a unique service name, the service identity and then the actual data inside of them and also quotas.

    This should also support multiple handles having access to an entry. Should be differentiated between read-only and read/write.

    #### Get Service Entries for Handle/User Identity

    #### Get Service Entry Details

    #### Create Service Entry
    Important is to define the service identity/handle, user/identity and the service name, which needs to be unique and is used for the local scope. Ideally a base service name + a random number + Also the Service URL ideally.

    #### Update Service Entry
    Set Quotas for Storage and maybe Table count
    Two distinct quotas, binary content and table data!
    Also updating access levels per handle.
    Also update the service name.

    #### Delete Service Entry
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
