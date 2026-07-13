# Goofy Protocol FIS (Federated Identity Server) Backend

WIP "Reference" Implementation of a FIS for Goofy Protocol.

## General Infos
(TODO)

## TODOs (Currently)
* Frontend Add a Settings Menu (Ideally position absolute icon in the corner) to always be able to set the Server and Storage Mode
* Backend Add Setting for User to set a custom frontend URL and use that when redirecting stuff.
* Also have the Backend Root Redirect set the Backend URL inside the Frontend, so that the Frontend contacts the correct backend, lol
* Have the Public Entry for the Identity support including paths for the actual entries (e.g: I have a public Goofy Media 2 Account on this identity and this is the service entry UUID + Table UUID / Name so that others can access stuff in a federated way!)
* Work on Frontend
* Work on Implementing API Endpoints + Services + DB Entities + FileStorage + DB Management + Config
* Work on more Implementation Stuff
  * Implement User Restriction
  * Implement User Account Deactivation
  * Implement User Account Deletion -> Should safely delete everything and not cause DB issues (Cache too)
    * Also potentially enforce having done an account-export within 7 days of trying to delete the account to avoid unwanted data loss
  * Add Speed Throttling for Large Downloads (for example for Data Export) to avoid DoS / Maybe using Bucket4j
  * Implement Data Export (How to treat Buckets and Tables?) (Probably export everything as a ZIP and assume the download should be ok)
  * Implement Data Import? (How to treat Buckets and Tables?)
* Implement Exceptions for unsupported Crypto Requests
* Add Home / Explanation Page to FIS Frontend
* Add Config for HandleCrypto Cache/Maps (size, expiration, etc)
* Add Max Unresolved Registration Requests and Reports Config + Error Codes
* Add Config for regular pruning of old unresolved Registration Requests
* Prepare Docker Stuff for hosting it and test#
* Create 2 FIS Instances (one main instance and one demo for others to try out, which is very limited but ppl can switch over or self host later)
* Create extra DB Table for known FIS Domains ?
* Look into ML-KEM using Seed for Private key and compatibility with JS (Potentially using Rust ML-KEM compiled to WASM) (Will have to see)

## TODOs (Later)
* Add Caching to relevant Endpoints with relevant durations (Handle Lookup, General Info, Maybe redirects, etc.)
* Add Quota Overviews for Users, showing all Storage related stuff for an entire user/identity (+ Useful for Admins having an overview)
* Create a sample Endpoint / document the potential runtime errors (Mostly in Signed Request filtering / Forbidden)
* Potentially add examples to the DTOs using annotations or so for swagger
* Maybe Add Config Parameter to disable Request Signatures (only for dev/test profiles) and create Bruno Workspace
* Create automated API Spec genertion in PDF/MD format
* Implement silly Rate Limiting (only for prod/develop), Base: https://www.baeldung.com/spring-bucket4j
  * Requests without a special cookies token will have to wait some time before their request is processed / get extra low prio / strong rate limiting, then they will get the special cookie
  * Ideally Requests without the special cookie dont even get their handle derived/checked and get put on a queue with max size (random elimination) or so to prevent DoS attacks
  * Requests with the cookie will get individual rate limiting based on their unique cookie (if valid) / maybe also based on the handle
* Move the Crypto Core Lib into a seperate package with tests, known values and pom.xml
* Look into Canonical Builds
* Improve CORS?
* Potentially overhaul the simplistic quota system and let users set quotas for specific entries too.
* Optimize Quota & Storage Calculations in Bucket and probably Table Service
* Add a simple "Notification" / Warning System that alerts Users when their storage quotas are about to be exceeded or if they have some content deleted or are restricted
* 


## Features
(TODO)

## Setup
(TODO)
* Clone the Repository
* Inside the `/src/main/resources` directory:
  * Change `application.properties` to use your wanted profile, probably `prod`
  * Copy the `application-prod.example.properties` to be `application-prod.properties`
  * Check the `application-prod.properties` and adapt/edit it to your needs
* Run the Application, it should create the DB and the needed tables automatically
* Get the Admin Register Code from the Logs and Register your Admin Account
* Profit?

## Notes
(TODO)

## Profiles
There are currently 3 Profiles:
* `dev` - Development Profile, used for local development and testing
* `prod` - Production Profile, used for production deployment
* `test` - Test Profile, used internally for executing tests

The dev and prod Profiles use different databases and the test Profile uses an in-memory database for testing purposes.

(TODO)

### File Storage
File Storage will also depend on the Profile used, for now its not implemented. 
To set the path I will probably use something like this:
```java
public FileStorageService(@Value("${app.storage.dir}") String dir) {
    this.baseDir = Path.of(dir);
}
```

The Test Data Path should also be fully reset on launch of the Test Profile, so that tests can be executed without any side effects from previous test runs.

## ROLES
(TODO)

## API Docs
The actual specs will can be found by starting the application in the `dev` Profile and checking http://localhost:8080/swagger-ui/index.html.

All Non-Admin Endpoints should be accessible and behave the same way on all FIS implementations, so that clients can be implemented in a generic way and work with any FIS implementation.
The Admin Endpoints don't necessarily have to be implemented in the same way, but it would make sense to have them implemented in a similar way, so that a generic client can be used for all FIS implementations.

I will at some point make the source include a PDF or Markdown file with the current API Specs but currently I've had issues with automatically generating those :(

Later on the version will be copied to the base goofy-protocol repository.

### Table/Bucket Access
(TODO)

### Error Codes
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
The Error Codes can be found [here](src/main/java/com/masl/goofy_protocol_fis_be/exception) in the `client` and `server` directories.



## Implementation Details
(TODO)
* Firstly work on the Crypto Core Lib, either porting it or creating it and then testing it properly (Known Value Tests are quite useful)
* Implement the Exception Handling System so that the Error Codes and Structures match
* Implement the Signed Request filtering and Role System
* Start implementing API Endpoints
* Start working on the Main DB, Entities and general Persistence
* Start working on the File Storage System & User DB Management
* Start working on the Config And Quota System
* Either use/adapt the reference Frontend Client or implement one yourself
* Work more on the Endpoints & Services
  * Root & General Endpoints
  * Registration & User Endpoints
  * Login Storage
  * Identity Storage
  * Service Entry
  * Service Bucket
  * Service Table
  * Redirects
  * Admin Endpoints
* Keep testing and use the reference implementation for the client and backend as help.






### Symmetric Cryptography
(TODO)

#### Supported Algorithms
Supported types can be seen in the `SymmCryptoType` Enum, currently they are:
* AES-128-GCM
* AES-196-GCM
* AES-256-GCM (**Recommended**)
* ChaCha20 (**Recommended**)

#### Symmetrically Encrypted Data Format
(TODO)

#### Algorithm Implementations
(TODO)

Supported Types:
* AES-128-GCM
* AES-196-GCM
* AES-256-GCM
* ChaCha20

For now, you can look into the [Implementations](src/main/java/com/masl/goofy_protocol_core/crypto/isolated/symm).







### Asymmetric (/Hybrid) Cryptography
(TODO)

#### Supported Algorithms
Supported types can be seen in the `AsymmCryptoType` Enum, currently they are:
* RSA 2048
* RSA 3072 (**Recommended**)
* RSA 4096 (**Recommended**)
* EC_P256
* EC_P384
* EC_C25519 (**Recommended**)
* ML-KEM (512) + ML-DSA (44)
* ML-KEM (768) + ML-DSA (65)
* ML-KEM (1024) + ML-DSA (87)

##### Important Notes
Currently, the support for `ML-KEM` & `ML-DSA` on Browsers is lacking, so for now it should be avoided until the support is better and I have implemented it in the JS Lib.

Additionally, `EC_P256` and `EC_P384` are currently not supported in the JS Lib either, and instead `EC_C25519` should be used, as thats the default anyway.


#### Public Split Key Format
(TODO)

`PUB.[TYPE].[SIG KEY].[ENC KEY].[ENC SIG]` / `PUB.[TYPE].[SIG KEY].X.X`

In more Detail:
```
[TYPE] - The Type of Algorithm(s) used for the Split Keypair
[SIG KEY] - The Public Key used for signing, Format: X509EncodedKeySpec as Base64 URLEncoded String
[ENC KEY] - The Public Key used for encryption, Format: X509EncodedKeySpec as Base64 URLEncoded String
[ENC SIG] - The Signature of the Public Key used for encryption, Format: Signature Bytes as Base64 URLEncoded String
```
Note: Some algorithms (RSA, EC) do not need a separate encryption key, so the `[ENC KEY]` and `[ENC SIG]` fields are both replaced with `` in those cases.
Technically, you can add them anyway but it wastes space and is not needed.

The Encoding Signature is used to verify that the Public Key used for encryption is valid 
and was generated by the same entity that generated the Public Key used for signing.


Examples:
```
RSA 2048 (~410 bytes)
PUB.RSA_2048.MIIBIjAt<...>IDAQAB.X.X

EC 256 (~140 bytes)
PUB.EC_256.MFkwEwYHK<...>fhckKxUUDcQ==.X.X

ML-KEM/DSA 512/44 (~6126 bytes)
PUB.MLKEMDSA_512_44.MIIFMjA<...>8EML0hY=.MIIDM<...>PJnu_MKWR.8dC<...>A0gMEA=
```

#### Private Split Key Format
(TODO)


#### Signature Format
Normally a Base64 URLEncoded String of the resulting Signature Bytes. 
The underlying Format depends on the Signature Algorithm used, which is defined in the Public Split Key.


#### Asymmetrically Encrypted Data Format
(TODO)


#### Algorithm Implementations
(TODO)

Supported Types:
* RSA 2048
* RSA 3072
* RSA 4096
* EC_P256
* EC_P384
* EC_C25519
* ML-KEM (512) + ML-DSA (44)
* ML-KEM (768) + ML-DSA (65)
* ML-KEM (1024) + ML-DSA (87)

Asymmetric Cryptography is usually used in a hybrid way, where the actual data is symmetrically encrypted and the symmetric key is asymmetrically encrypted with the public key of the recipient.
The Implementations rely on the Symmetric Cryptography Implementations for the actual data encryption and decryption.
Currently, by default `AES_GCM_256` is used for the symmetric encryption of the data.
One outlier for the statement above is the Asymmetric Encryption using P-256/384, as it uses `ECIESwithAES-CBC` directly.


For now, you can look into the [Implementations](src/main/java/com/masl/goofy_protocol_core/crypto/isolated/asymm).








### User Handle
The User Handle is a unique identifier for a user (or rather a keypair) in the Goofy Protocol. 
It is used to identify a user and their associated public split key and is globally unique across all domains.

The handle is tightly coupled to the public key by being derived from it, so it is not possible to change the handle without changing the public key and vice versa.
This also means that the handle can always be verified to be correct by deriving it from the public key and comparing it to the handle.

The specific derivation is specified below in more detail.

Format: `[word]_[word]NNNNN` / `[word]_[word]_[word]NNNNN` / `[word]_[word]_[word]_[word]NNNNN`

In more Detail:
```
[word] - A Word chosen from the List in `handle_words.json (there can be 2-4 words in a handle)`
NNNNN - A Number from 0 to 99999
```

Example: `beray_drubs_pant57107`

#### Domain Parts
Usually, handles do not have the domain attached and shouldn't be stored as one string with the domain attached.
This is because the handle should be portable and not tied to a specific domain.
Of course the current domain for a handle should be stored, just separately and only used when needed. (For example looking up the public split key)

A username with an attached domain has the following format:
`[handle]@[domain]`

Example: `beray_drubs_pant57107@fis.rocc.systems`

NOTE: When sending a signed request with only your handle, it is advised to attach the domain, if theres a chance the Server doesn't know it yet.
If the server cannot resolve your handle, it will throw an error and ideally your client would send the handle with the domain attached.

NOTE: The domain technically allows to have the port defined too, useful for testing with localhost.


#### Cryptographic Handle Derivation
(TODO)


See [Example Implementation](src/main/java/com/masl/goofy_protocol_core/crypto/connected/HandleCrypto.java) for a working example of the Handle Derivation.



#### Strength
(TODO)
```
// Strength of handles
// c = 2 -> ~44 bit (15000^2 * 10^5 = 2.3e13)
// c = 3 -> ~58 bit (15000^3 * 10^5 = 3.4e17)
// c = 4 -> ~72 bit (15000^4 * 10^5 = 5.1e21)
```




### Signed Requests
(TODO)

#### Parts
(TODO)

#### Headers
The following headers are needed for Signed Requests:
* `X-Goofy-Public-Key`: The Public Split Key of the Sender (format defined above)
* `X-Goofy-Handle`: The Handle of the Sender (format defined above, can have a domain attached)
* `X-Goofy-Signature`: The Signature of the Request (format defined above)
* `X-Goofy-Id`: A random Id in the form of a Long (64bit) Integer, used to prevent replay attacks (The server wont store them forever usually)
* `X-Goofy-Valid-Until`: A timestamp in the form of a Long (64bit) Integer representing the time in milliseconds since epoch, used to enforce a time limit on the validity of the request

The default validity of a Signed Request should be 60 seconds. This is because surprisingly a lot of devices arent closely synchronized with the actual time and can be off by some time. (Sometimes even multiple minutes)

#### Signature
(TODO)

Servers should reject requests with a valid until timestamp, which has already passed or is too far in the future (for example >1h).

#### Validity
(TODO)


#### Signature Sizes
Below are some rough measurements of the average added size of the total headers using Signed Requests.

Using the Public Split Key (usually larger)
```
RSA 2048:                       ~900 bytes
RSA 3072:                     ~1,200 bytes
RSA 4096:                     ~1,500 bytes
EC_P256:                        ~350 bytes
EC_P384:                        ~400 bytes
EC_C25519:                      ~400 bytes
ML-KEM (512)  + ML-DSA (44):  ~9,500 bytes
ML-KEM (768)  + ML-DSA (65): ~13,200 bytes
ML-KEM (1024) + ML-DSA (87): ~18,100 bytes
```

Using Handles only (in general smaller)
```
RSA 2048:                       ~500 bytes
RSA 3072:                       ~600 bytes
RSA 4096:                       ~800 bytes
EC_P256:                        ~200 bytes
EC_P384:                        ~250 bytes
EC_C25519:                      ~200 bytes
ML-KEM (512)  + ML-DSA (44):  ~3,350 bytes
ML-KEM (768)  + ML-DSA (65):  ~4,500 bytes
ML-KEM (1024) + ML-DSA (87):  ~6,300 bytes
```

### Login Storage
(TODO)
The username is hashed with sha256 and encoded using Base64URL.

### Identity Storage
(TODO)

### Service Entry
(TODO)




### User Tables
(TODO)

#### Table Structure
(TODO)

#### Table Access
(TODO)

#### Table Permissions
(TODO)

#### Table Creation
(TODO)
Important: Supported Datatypes, limits for columns, column names, primary key, foreign keys? (on update/delete?) (interop with different tables?), custom field indexing?  

#### Table Deletion
(TODO)

#### Table Size


#### Table Query Idea
(TODO)

to query a table, users can send queries as a JSON object:
```json
{
  "select": [],
  "where": [],
  "having": [],
  "groupBy": [],
  "orderBy": [],
  "limit": 0,
  "offset": 0
}
```

Keep in mind this will still be quite limited and only support a small subset of SQL queries, but it should be enough for most use cases.

The datatypes of the tables will also be limited.

All fields except for the `select` field are optional and can be omitted if not needed.

The `select` field will contain a list of either pure columns or aggregate functions like `COUNT`.


The `where` field will contain a list of conditions applied to the columns but also can have nested conditions (for example with `AND` `OR` `NOT`).
The conditions will be have a limit to prevent abuse.

The `having` field will contain a list of conditions applied to the aggregate functions but also can have nested conditions (for example with `AND` `OR` `NOT`).

The `groupBy` field will contain a list of columns to group the results by.

The `orderBy` field will contain a list of columns to order the results by, with an optional direction (ASC/DESC).

The `limit` field will contain a number to limit the number of results returned.

The `offset` field will contain a number to offset the results returned, useful for pagination.


An example query could look like this:
```json
{
  "select": ["id", "name", "COUNT(*)"],
  "where": [
    {
      "column": "age",
      "operator": ">",
      "value": 18
    },
    {
      "column": "country",
      "operator": "!=",
      "value": "USA"
    }
  ],
  "groupBy": ["country"],
  "orderBy": [
    {
      "column": "name",
      "direction": "ASC"
    }
  ],
  "limit": 10,
  "offset": 0
}
```

This is for querying, for inserts/updates/deletes, the system might be different.

