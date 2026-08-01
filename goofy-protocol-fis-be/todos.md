# TODOs
This is a list of open TODOs.

## TODOs (Currently)
* Implement Admin Endpoints
  * User Management (List Users, Delete User, Deactivate User, etc.)
* Work on more Implementation Stuff
    * Implement User Account Deactivation
    * Implement User Account Deletion → Should safely delete everything and not cause DB issues (Cache too)
        * Also potentially enforce having done an account-export within 7 days of trying to delete the account to avoid unwanted data loss
    * Add Speed Throttling for Large Downloads (for example for Data Export) to avoid DoS / Maybe using Bucket4j
    * Implement Data Export (How to treat Buckets and Tables?) (Probably export everything as a ZIP and assume the download should be ok)
    * Implement Data Import? (How to treat Buckets and Tables?)
        * When doing exports/imports of a table, don't use raw db files. Also when importing large things look into how to best do it, given the upload limits
* Add Synchronised Block for Post & Put Operations to avoid Race Conditions (Especially for Bucket & Table Stuff and everything quota related!!)
    * Ideally have a synchronized block with a lock per relevant Object (User, Service Entry / Bucket / Table, Table Entry, etc.)
    * Should mark the Endpoints as `@Transactional` and either use a `ReentrantLock` or a general Lock for (e.g) the Service UUID or the `@Lock` annotation.
* Implement Exceptions for unsupported Crypto Requests
* Add Home / Explanation Page to FIS Frontend
* Add Config for HandleCrypto Cache/Maps (size, expiration, etc.)
* Add Max Unresolved Registration Requests and Reports Config + Error Codes
* Add Config for regular pruning of old unresolved Registration Requests
* Host my main instance on my server and host a limited (maybe periodically resetting) demo instance on my VPS
* Create extra DB Table for known FIS Domains ?
* Look into ML-KEM using Seed for Private key and compatibility with JS (Potentially using Rust ML-KEM compiled to WASM) (Will have to see)

## TODOs (Later)
* Add a simple "Notification" / Warning System that alerts Users when their storage quotas are about to be exceeded or if they have some content deleted or are restricted
* Potentially look into having custom JWTs or something to avoid signed request overhead when accessing tables?
* Potentially rename all Endpoints from `/api/...` to `/fis-api/...` to avoid potential conflicts/request forgeries from service that have the same endpoint/path and therefore could redirect the requestt to the FIS
  * Shouldn't realistically happen and the client should be a bit "careful", but it would probably be a good idea anyway.
* Change some FIS Exceptions to use more detailed HTTP Error Codes (404 for not found, etc.)
* Add Caching to relevant Endpoints with relevant durations (Handle Lookup, General Info, Maybe redirects, etc.)
* Look into indexing columns for performance in user DBs
* Add Quota Overviews for Users, showing all Storage related stuff for an entire user/identity (+ Useful for Admins having an overview)
* Create a sample Endpoint / document the potential runtime errors (Mostly in Signed Request filtering / Forbidden)
* Potentially add examples to the DTOs using annotations or so for swagger
* Add Exporting User Keypair with a password + Importing a password encrypted Keypair (For safety reasons)
* Allow Securing your Fis Frontend Client with a password too, (Encrypted Local Storage)
* Maybe Add Config Parameter to disable Request Signatures (only for dev/test profiles) and create Bruno Workspace
* Create automated API Spec generation in PDF/MD format
* Add Swagger examples for DTOs
* Implement silly Rate Limiting (only for prod/develop), Base: https://www.baeldung.com/spring-bucket4j
    * Requests without a special cookies token will have to wait some time before their request is processed / get extra low prio / strong rate limiting, then they will get the special cookie
    * Ideally Requests without the special cookie don't even get their handle derived/checked and get put on a queue with max size (random elimination) or so to prevent DoS attacks
    * Requests with the cookie will get individual rate limiting based on their unique cookie (if valid) / maybe also based on the handle
    * Add to all relevant endpoints
    * Check if that works well if the clients use fetch requests, then idk if the cookies will be set for the request
* Move the Crypto Core Lib into a separate package with tests, known values and pom.xml
* Look into Canonical Builds
* Potentially overhaul the simplistic quota system and let users set quotas for specific entries too.
* Optimize Quota & Storage Calculations in Bucket and probably Table Service
* Look into how to handle a whole FIS instance moving to a different domain
* Maybe add Access Logs to Bucket and Table entries (like last 10000 access or so) for reporting purposes