
## Admin API
These API Endpoints should only be accessible by signed requests which are authorized to be an administrator of the FIS.

They do not need to 100% implement all the Endpoints by the spec but it would make sense to have these endpoints. (Especially if using a general (statically hosted/local) client)

### General
You can query between user signed and guest requests.
And also by status (open / done / flagged)

### User Management
View User Details, Delete Users, send warnings, etc.

Also promote/demote Admin status

Also see all handles of a user and maybe manage the quotas?


### Encrypted Password/Keypair Storage Management
Manage the Encrypted Storage. (View all entries, delete entries)

See all entries for a user maybe, stats, idk yet.


### User Service Management
Managing Services for a handle


### User Service Table Management
Managing Tables from Services for a handle

### User Service Bucket Management
Managing Buckets from Services for a handle


### Service Management
View Services that are used, maybe some stats, have a blocklist of server.



### Stats
Stats for user and service access and storage quotas.

Setting the total FIS storage quota


### Backup
Backup related things


#### Export Full Backup


#### Import Full Backup







## Potential for future improvement

If FISs get large there should be some moderation users with some kind of abilities but not quite administrators.

Look into locking Buckets maybe?