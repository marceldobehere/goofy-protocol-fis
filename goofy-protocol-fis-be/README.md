# Goofy Protocol FIS (Federated Identity Server) Backend
WIP "Reference" Implementation of a FIS for Goofy Protocol.

## General Infos
The FIS plays a central role in the Goofy Protocol, as it is the main point for storing and managing all data related to a user and their identities.

It also acts as a sort of identity provider, allowing services to check the validity of a user and their identities, as well as providing access to the user's data.

The concept is kind of similar to a [Matrix Homeserver](https://matrix.org/homeserver/about/) and a [Solid Pod](https://solidproject.org/about), but with a focus on cryptography and security, as well as being compatible with the Goofy Protocol.

Anyone can host their own FIS instance and have their own users and identities, as well as their own services, which should be compatible with all FIS instances.


A rough explanation on the Goofy Protocol and its pros/cons can be found [here](explanation.md).

## Notes
This is still very WIP, and I'm only now starting to reach the stage where i will be able to host a test instance and start working on test services.


## Features
* User Registration & Login
* Identity Management
* Service Entry Management
* Bucket & Table Management with Quotas
* Admin Management
* Cryptography Support (Symmetric & Asymmetric)
* Strongly Documented API Specs (Swagger UI)
* Compatible with the Goofy Protocol


## Basic Layout
The main components of the FIS are:
* Crypto Core Lib (Crypto Core Library)
  * Internally using BouncyCastle
* Backend (Spring Boot Application)
* Main DB (H2 + JPA)
* Storage System (File Storage + User DBs) (Currently using the local FS)
  * User DBs (H2)

## Setup
(TODO)
* Clone the Repository
* Inside the `/src/main/resources` directory:
  * Change `application.properties` to use your wanted profile, probably `prod`
    * Only if you're not using docker compose / don't want the default dev profile
  * Copy the `application-prod.example.properties` to be `application-prod.properties`
  * Check the `application-prod.properties` and adapt/edit it to your needs
  * Copy the `docker-compose.example.yml` to be `docker-compose.yml` and adapt it to your needs
* Run the Application, it should create the DB and the needed tables automatically
  * Either via `mvn clean spring-boot:run` or via `docker compose build && docker compose up
* Get the Admin Register Code from the Logs and Register your Admin Account
* Profit?


## TODOs
The TODOs can be found [here](todos.md).


## Profiles
There are currently 3 Profiles:
* `dev` - Development Profile, used for local development and testing
* `prod` - Production Profile, used for production deployment
* `test` - Test Profile, used internally for executing tests

The dev and prod Profiles use different databases and the test Profile uses an in-memory database for testing purposes.


## ROLES
There are currently 5 Roles defined in the system: (They are not mutually exclusive)
* `OUTSIDE_ENTITY` - Any Outside Entity, which has a valid signed request 
* `REGISTERED_USER` - A registered User
* `REGISTERED_IDENTITY` - An Identity of a registered User
* `ADMIN` - An Administrator
* `RESTRICTED` - A User which has been restricted by an Admin, the user is basically set into a read-only mode


## API Docs
The actual specs can be found by starting the application in the `dev` Profile and checking http://localhost:8080/swagger-ui/index.html.

All Non-Admin Endpoints should be accessible and behave the same way on all FIS implementations, so that clients can be implemented in a generic way and work with any FIS implementation.
The Admin Endpoints don't necessarily have to be implemented in the same way, but it would make sense to have them implemented in a similar way, so that a generic client can be used for all FIS implementations.

I will at some point make the source include a PDF or Markdown file with the current API Specs, but currently I've had issues with automatically generating those :(

Later on the version will be copied to the base goofy-protocol repository.


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


## Implementation "Guide"
If you plan to write your own implementation of a FIS, you can look [here](impl.md) for a rough guide on how to implement it.

### Cryptography
The FIS supports the main crypto algos outlined in the Goofy Protocol, which currently are:
* Symmetric
  * AES-128-GCM
  * AES-196-GCM
  * AES-256-GCM (**Recommended**)
  * ChaCha20 (**Recommended**)
* Asymmetric 
  * RSA 2048
  * RSA 3072 (**Recommended**)
  * RSA 4096 (**Recommended**)
  * EC_P256
  * EC_P384
  * EC_C25519 (**Recommended**)
  * ML-KEM (512) + ML-DSA (44)
  * ML-KEM (768) + ML-DSA (65)
  * ML-KEM (1024) + ML-DSA (87)

The details of the implementation alongside other parts can be found [here](crypto.md).


### User Handle
The details can be found [here](handle.md).

### Login Storage
(TODO)
The username is hashed with sha256 and encoded using Base64URL.

### Identity Storage
(TODO)

### Service Entry
(TODO)

### User/Service Storage Details (Tables & Buckets)
The details can be found [here](storage.md).


