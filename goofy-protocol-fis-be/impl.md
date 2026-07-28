# Implementation "Guide"
(TODO) WIP!!

If you plan to write your own implementation of a FIS, you can use this as a reference implementation and look into the code to see how things are implemented.
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