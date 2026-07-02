# Minimal Specs for a valid FIS
These are the minimal specs for a valid FIS.

These are still very WIP.


# Table of Contents
- [Minimal Specs for a valid FIS](#minimal-specs-for-a-valid-fis)
- [Table of Contents](#table-of-contents)
- [Specs](#specs)
    - [Error Codes](#error-codes)
    - [API](#api)

# Specs


## Error Codes
For now, Errors are split into ClientErrors and ServerErrors, which all use unique Error Codes and have the following structure:
```
{
    "errorCode": <INT>,
    "message": <Message>,
    "details": {
        <Details depending on exact error>
    }
}
```
The Error Codes can be found [here](/src/main/java/com/masl/goofy_protocol_fis_be/exception) in the `client` and `server` directories.


## API

### General
Endpoints for general stuff about the FIS.

#### Info
Returns information about the fis instance

#### Contact
These can be used publically but can also be signed by a user.

#### Report
These can be used publically but can also be signed by a user.

#### Public Handle / Key
Get the public handle / public key of the fis server itself


### Verification / User Lookup
Endpoint(s) for getting information about a handle

TODO: Make sure that all relevant endpoints notify when a handle has been moved

#### Handle Lookup
Looking up the base / service identity using the handle and getting some basic information back. (Handle, Public Key, which FIS server it is on, etc, if its from the fis service?)

#### Set/Update (Ext.) Handle Information
TODO: Finalize, and update other fis endpoints (data/service related)

Users from other FISs or users that move their account should be able to update the fis server url and other things for their handle, this is important because of the implicit nature of the handles and services having the old fis cached.

### Registration (Base Identity / Account)
Endpoints for registering new profiles

#### Check Registrations Allowed
Checks if the FIS even allows registrations currently

#### Contact
A way to contact if you want a register code. If register codes are automatically sent by email or something, this can be skipped.

#### Create Registration
This endpoint is used to create a new account. It should be a signed request using the generated keypair and include the public key and handle of the registering user. It should also include the registration code/token/key/etc.

### Login/Check (Base Identity / Account)
Endpoint(s) to get data for signed request and also as a check if the signature is working.

#### Get User Info
This signed request will return info about the user who sent it. This can be used to check whether the signing works, if the user is authenticated and if the user is an administrator.


### Encrypted Password/Keypair Storage (Base Identity / Account)
Endpoints for storing encrypted keypairs, which are used for password based login.

Each registered user can have one entry with a chosen username (if available) and can store text data in it. The entry can be looked up publically with the username as the key.

The username will never be sent in plaintext, it will be hashed beforehand so that the FIS does not know the username and only the user knows it. (As an additional layer of security)

It's intended for users to symmetrically encrypt their keypair with a password and store that in this storage. (If want to store it conveniently)

The Entry should have a normal size constraint, maybe max 100KB.

#### Get Storage Entry for Username
Gets the Storage entry for a username or a 404 if it doesnt exist


#### Post Storage Entry for Username
Sets the Storage entry for a username, if the username is free.
Request needs to be signed and a user can only have one username at once, if its a different one, then the old one will be deleted.


#### Delete Storage Entry for Username
Request needs to be signed.



### Encrypted (Service) Identity Storage
Endpoints for storing and retrieving generated Identities. They will be stored encrypted. Of course only used by registered users.

The Entries are symmetrically encrypted with a key.
This key is stored alongside the entry but it is asymmetrically encrypted with the public key of the base identity, meaning only the user can decrypt/use them.

The identity entries should have the handle and a custom name added to them which doesnt need to be encrypted and a string for notes which should be encrypted.

The Entries should have a normal size constraint, maybe max 100KB. There should also be a maximum number of encrypted identities, but that depends on the FIS, I would say at minimum 20 should be good.

#### Get All Identities
This will return a list of all identities. Will only have custom names and notes and maybe handles.

#### Get Specific Identity
This will return all data for a single identity.
The data should be used to be able to export the identity.

#### Create/Update Identity
This will allow users to create/update identity entries for a custom name.

You should be able to import an identity, ideally with all the data of the `Get Specific Identity` Endpoint.

#### Delete Identity
This will delete an identity entry for a custom name.


### Global Handle Information
For each identity the user has, they can set global public and private data.
This data is basically just a JSON Object with keys and values.


Public Data for example should include a `services` key which has an object with service names and the urls of the service instance the handle is used on. This can be useful if you use the same handle for several services and want others to find the instances.


#### Get Public Data


#### Post Public Data


#### Get Private Data

#### Post Private Data




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







### External Service Access
These Endpoints get used by services.

Basically for a user there are two things:
* Services connected to a identity (Used for login)
    * Basically just a list of services and identities
    * Useful so you can "autofill" the "login" credentials for a service
* Service Data Access, so giving a service permission to create/read/write tables and buckets. Each Service Data Access will have the service and the handle and then a local scope name,


#### Get Service name


#### (REDIRECT) Add Service Access to Handle
This should be a URL that services can link users to which should redirect users to a (ideally) Frontend Page of the FIS where a logged in user can add the service (defined in the query parameter) to a handle.

This is there to make the userflow easier.

This can be used to setup Service Entry / Basic Data Access with Tables / Buckets but also without, just to have the service be "connected" with the user/identity so that it can be used to login.


#### (REDIRECT) Get Service Login Credentials / Keypair
This should be a URL that services can link users to which should redirect users to a (ideally) Frontend Page of the FIS where a logged in user can get all handles associated with the service (defined in the query parameter) and can get the keypair which can be used to log in.

It could potentially add a link to the service which would get the credentials in the url (ideally encoded inside the fragment `#` portion for security reasons) so that the user can get signed in by just selecting the handle and getting redirected to the specific service url.

This is there to make the userflow easier.


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




### DSGVO / GDPR
These endpoints should be signed by the user. If someone without a signature wants something, they should use the public contact or report endpoint

#### Get Complete Account Export

#### Delete Account and all Data

#### Deactive Handle or so

#### Move Handle to different FIS or so

#### Import Export of different FIS?
If you want to move your data to a different FIS you can export it and register somewhere else and import it there hopefully.

Can also make the current FIS point to the new one

Need more details as to like how itll work and with the base identity


### Account Information

#### Get Storage Details

#### Get Storage Quotas

#### A







## Admin API
These API Endpoints should only be accessible by signed requests which are authorized to be an administrator of the FIS.

They do not need to 100% implement all the Endpoints by the spec but it would make sense to have these endpoints. (Especially if using a general (statically hosted/local) client)

### General
You can query between user signed and guest requests.
And also by status (open / done / flagged)

#### Get Contact Requests

#### Update Contact Request
Set it to Open/Done/Flagged and also allow for custom notes.

#### Delete Contact Request

#### Get Report Requests

#### Update Report Request
Set it to Open/Done/Flagged and also allow for custom notes.

#### Delete Report Request


#### Set Public FIS Instance Info Data



### Registration Management

#### View Registration Codes
View used and unused registration codes

#### Create Registration Code
Create a registration code for a user / admin

#### Delete Registration Code


#### Set Registrations Allowed


#### Get Registration Code Requests

#### Update Registration Code Request
Set it to Open/Done/Flagged

#### Delete Registration Code Request


### User Management
View User Details, Delete Users, send warnings, etc.

Also promote/demote Admin status

Also see all handles of a user and maybe manage the quotas?

#### A


### Encrypted Password/Keypair Storage Management
Manage the Encrypted Storage. (View all entries, delete entries)

See all entries for a user maybe, stats, idk yet.

#### A



### User Service Management
Managing Services for a handle

#### A


### User Service Table Management
Managing Tables from Services for a handle

#### A

### User Service Bucket Management
Managing Buckets from Services for a handle

#### A


### User Storage Quota Management

TODO: Define behaviour on what should happen if an admin shrinks the quota for a user. What should get shrunk and what if there already is too much data for the quota? Force Delete? Send the user a notice and give them a month to download their data or to fix their quota?

Two distinct quotas, binary content and table data!

How much storage can be used / is used, and how many tables can be created / have been created. Also how many columns/rows a table can have.

#### B


### Service Management
View Services that are used, maybe some stats, have a blocklist of server.


#### B







### Stats
Stats for user and service access and storage quotas.

Setting the total FIS storage quota

#### B


### Backup
Backup related things


#### Export Full Backup


#### Import Full Backup







## Potential for future improvement

If FISs get large there should be some moderation users with some kind of abilities but not quite administrators.

Maybe include rate limiting suggestions for the endpoints

Look into synchronised code blocks / locking table accesses to avoid weird issues

Look into locking Buckets maybe?