
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