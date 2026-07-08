package com.masl.goofy_protocol_fis_be.rest.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service-table")
@Tag(name = "Service Table Access", description = "Endpoints related to accessing Tables.")
public class ServiceTableEndpoint {
    /*
    ### Service Table Access
    These Endpoints get used by services to access the tables.
    IMPORTANT, KEEP SCOPE IN MIND: EITHER LOCAL (Uses service name as a prefix) or GLOBAL (Uses absolute name, for example if a service wants to access data from a different service, for example a chat app server writing received messages to its table and the client reading the entries from the table and storing it in its own table)

    Maybe look into MultiTenancy for Java/Databases or something for implementing it


    #### Get Tables
    Think about showing all tables depending on global or local scope.

    #### Get Tables Quota
    How much storage can be used / is used, and how many tables can be created / have been created. Also how many columns/rows a table can have.

    #### Get Table
    Column Definitions and stuff
    How many entries, how much storage used, etc.
    Is the Table publically visible.

    #### Create Table
    Define Colums
    Define Visibility

    #### Set Table Visibility
    Basically is the Table only visible by handles with access or to everyone (private service data or public social media post entries)

    #### Delete Table


    #### Locking a Table
    If several entities are trying to access a table at the same time, it could cause issues, that is why you can lock a table for everyone but you.

    You can set the lock to be write only or read write depending on your needs. You also will have to add a duration to lock the table for.

    Tables can and should of course be unlocked early but should have a timeout to avoid possible race conditions. The timeout should have an upper bound, of for example 15-30s which should be clearly documented.

    Locking a table will return a lock token if successful and the other endpoints will be locked, unless you provide the lock token in the headers.


    #### Unlocking a Table
    Unlocking a table can only be done by having the lock token. Otherwise it will unlock automatically after the timeout.


    #### Get Table Entries

    #### Add Table Entry

    #### Update Table Entry

    #### Get Table Entry

    #### Delete Table Entry


    #### Query Table Entries
    Some kind of way to query tables with specific column constraints and also define what data/columns you want. should be basic but make life easier and increase performance.

    FIS for queries maybe yoink some stuff from drizzle orm? Select([])from().where().sort() etc
    */
}
