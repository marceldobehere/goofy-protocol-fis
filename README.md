# Goofy Protocol FIS (Federated Identity Server)

This is a (WIP) Reference Implementation of a FIS for the [Goofy Protocol](https://github.com/marceldobehere/goofy-protocol).
I am also using this to host my own Demo FIS Instance. (and later my Main Instance)

## Relation to the Goofy Protocol
The FIS plays a central role in the Goofy Protocol, as it is the main point for storing and managing all data related to a user and their identities.

It also acts as a sort of identity provider, allowing services to check the validity of a user and their identities, as well as providing access to the user's data.

The concept is kind of similar to a [Matrix Homeserver](https://matrix.org/homeserver/about/) and a [Solid Pod](https://solidproject.org/about), but with a focus on cryptography and security, as well as being compatible with the Goofy Protocol.

A rough explanation on the Goofy Protocol and its pros/cons can be found [here](./goofy-protocol-fis-be/explanation.md).



## Structure
This Repository contains the [Java Spring Backend](./goofy-protocol-fis-be/) and the static [NextJS Frontend](./goofy-protocol-fis-fe/).

This Repository also (currently) contains the source of the (Goofy Protocol) Core Crypto Library for Java.

The Demo Instance is currently hosted here: https://demo.fis.rocc.systems

If you want to try it out, please read [this section](#how-can-i-try-it)!


## Features
* User Registration & Login
  * Register using a Register Code & Request one
  * No need for personally identifying Data 
    * (outside of any mean to be contacted)
* Identity Management
  * Create & manage several isolated Identities for Services
  * Export the Keypairs to use in the Services **(Will be improved)**
  * Export the entire Identity along with all the data **(TODO)**
  * Import an exported copy of an Identity **(TODO)**
  * Identities are stored encrypted at rest & E2EE
* Service Entry Management
  * Create and Manage Service Entries
  * Export and Import all Data **(TODO)**
  * FIS has isolated DBs and Bucket Folders for every user
* Bucket & Table Management with Quotas
  * Manage All data that services/clients are storing on your FIS!
  * Delete, Modify and Insert Data
  * Support for Binary Files (in Buckets) and structured data (in Tables)
    * Full H2 DB with REST Endpoints for accessing/querying it
    * Granular Permissions for other users/services/handles
  * Data is centralized -> Easy synchronization!
* Useful General Stuff
  * Report Issues
  * Get Server Information
  * Get Contact Information for the Instance Owner
* Admin Management
  * Manage Users, Registrations, Reports, etc
  * Set granular Quotas for Users or use defaults
  * Ability to view User Tables/Buckets (if unencrypted) and delete if necessary!
* Easy to self-host and run locally
  * Run locally with just Java and NodeJS
  * self-host using docker
* Taking advantage of the Goofy Protocol
  * Service and FIS Instances can be fully decentralized
  * Users have full Sovereignty over their data!
  * Automatic globally unique UserIDs/Handles
  * Automatic Support for E2E Symmetric & Asymmetric Encryption
  * Strongly Documented API Specs (Swagger UI)



## How can I try it?
To try out the deployed demo FIS Instance, you will need a Register Code!
(You can also self host / run it locally if you want to)

Deel free to contact me for a code [here](https://rocc.systems/contact/). You can also Request a Register Code in the Client. (Currently I haven't implemented Notifications so I don't really see it lol)

You can then just generate a keypair on the page (or import one if it matches the format), enter the Registration Code and complete the registration process!

(Make sure to export the keypair and keep it save, as it is your only way of accessing your account)

Once you are done, you will be sent to the home page and can experiment around!


### What can I do?
Note, the FIS isn't very glamorous/fancy, it is mostly there for managing Identities, Service Entries and in general just your data. Normally you won't have to interact with it much, outside of setting up identities or managing data.

In general you can look at your `Identity Storage` and create/manage Identities that can be used for different Services (using the Goofy Protocol).

I would recommend creating one for the [Goofy IRC](https://github.com/marceldobehere/goofy-protocol-example-service#how-can-i-try-it) and giving it a try!

You can also Manage your Identities and see the Service Entries. (Normally a Service will create one and then use the service or your client will use that for storing data)

For every Service Entry you can manage the Buckets (Basically raw File Storage) and the Tables (Basically a custom small Database for each Entry)!

For example, you could try making a Service Entry, opening the Bucket Management Page and Upload a random File. You can then share that with others.

Or better, once you have a Service connected to your FIS, you can explore all the stored data!



## Notes
You should never trust strangers Goofy Protocol FIS or Service Clients and always verify the code first, or host the frontend yourself / run it locally! Do not give untrusted services keypairs that have important data linked to them!

The frontend currently temporarily stores your keypair in localStorage, I will add support for storing it encrypted at rest at a later point. (For now you can choose to store it in Session Storage or use it in a safe environment / don't do sensitive stuff)

Anyone can host their own FIS instance and have their own users and identities, as well as their own services, which should be compatible with all FIS instances.


## Resources
* [Goofy Protocol](https://github.com/marceldobehere/goofy-protocol) (WIP/TODO, Very messy)
* [Goofy IRC (Example Service)](https://github.com/marceldobehere/goofy-protocol-example-service)
