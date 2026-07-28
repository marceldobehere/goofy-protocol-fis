# Explanation
This is a rough explanation of the Goofy Protocol and what it is trying to achieve.

## (Personal) Issues with current stuff
Currently, there are already Federated Services for different things (Social Media like Mastodon, Chats like Matrix, etc.) but there are a few issues:
* The Services work very differently and are not really compatible with each other
* Cryptographic algorithms, parameters, practices and standards and usage vary a lot between different services
* Users cannot necessarily have the same identity (or even handle/username/tag format) across different services
* Most of the identities are bound to a specific Domain and do not directly have anything (Crypto Keys, etc.) linked to them
* The identities are not really portable, so if a user wants to switch to another service, they have to create a new identity and start over, losing all their data and connections
    * Additionally, you often need to provide your email or something to every single service, which is not ideal for privacy and security
* User Data for Services is also not usually portable, which makes moving/importing/exporting all data painful
* User Data for Services is stored in different formats in different locations, usually at the Service itself, which makes managing all of your data more difficult
    * Additionally, if for a service you store all data client side, synchronization across devices can get complicated and have issues
* The hosting of some of the services can be complicated and cumbersome, even more if you have performance in mind
    * As well as needing to set up and provision plenty of storage for every single service and managing everything separately

E2EE Encryption is already well done by applications like Signal and Matrix, though things like Matrix can be lacking in UI/UX and not intuitive.
There are also projects related to Federated Storage, like Solid Pods or IPFS.
But those all solve one part of the problem, while not being directly compatible and still have problems related to global identities.


## What does Goofy Protocol offer
Goofy Protocol defines identities/handles to be globally unique (but human-readable with hopefully enough bits to avoid collision) and directly bound to a cryptographic identity (public keys).
This means that a user can have the same identity across different services without issues.
Additionally, it means that an identity is no longer tied to a domain or service, which allows portability and freedom for the user to move between different domains.

The identities as well as cryptographic algorithms and parameters are defined in a standard way, which is compatible across services and platforms.
Also having (wip) support for Post Quantum Cryptography out of the box.

As for data storage, Goofy Protocol defines the FIS (Federated Identity Server) to be a central point for storing and managing all data related to a user.
The FIS stores all data for a user (Full Identity Keypairs (encrypted at rest), Public Data, Tables for structured data and Buckets for files) and allows the user to access/manage that data as well as share access with other users and services.
This means that different services should be built to let users decide where their data is stored and how it is shared, while still being compatible with other services.
The FIS also allows for several identities to be managed by a user, to allow for privacy/isolation to the services themselves. (Or can use different FIS instances too)

If a person wants to host several services for a group of people, they can host their own FIS and have the services check the identities against the FIS.
This means that for example only users who registered on the group FIS can use the services and also don't need to have different credentials across the services.


## Downside of Goofy Protocol
There are some downsides / tradeoffs that need to be considered:
* The identity is not bound to a domain/service but to a keypair, similar to a cryptowallet
    * If you completely lose your keypair, you lose your identity. You cannot do anything with it. (You can of course talk to the FIS owner and at least try to get an export your data)
    * If your keypair is compromised, then your identity is compromised and the only thing you can do is deactivate/delete your account
    * Since your keypair is mapped to a cryptographic keypair, it is fixed to that
        * This means that the Algorithm used for your identity cannot be changed without changing your identity.
        * This is relevant for the choice of algorithm as well as post quantum security
* The FIS of your choice will be the central store of your data
    * If the FIS stops working, your data could be lost. (Should have exports/backups)
    * If the FIS is compromised or malicious, then data could be deleted, manipulated or shared.
        * Private data should be encrypted with your own keypair
        * Important public data should be signed with your own keypair
        * If you handle your data safely, the only problem could be data loss. Only use FIS instances you trust
    * Some FIS Instances can and realistically will impose limits (Data Storage, Quotas, etc.) which could be too limiting for you.
    * Keep in mind, you should always be able to move to a different instance if you want to
* Your identity keypairs get used a lot for all sorts of activities and could potentially be accessible during the runtime of clients
    * You should trust your device and make sure it is not compromised
    * You should only use Service & FIS clients that you trust and can check the code of. Ideally statically hosting them yourself
    * If you don't fully trust a Service / Client, you should use a separate/isolated identity for it


There has been some thought put into having a Root Identity Keypair, which is only used to sign a temporary keypair for use in cryptographic stuff, which would shift the issues about keypair algos being tied to one single identity and the issue with compromising to the Root Identity, which could be a large PQC Keypair for maximum security.
Though this introduces a lot of extra complexity and overhead so it is not implemented in this system for now. Maybe at some future point it will.
