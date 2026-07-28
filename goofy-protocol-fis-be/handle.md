# User Handle
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

## Domain Parts
Usually, handles do not have the domain attached and shouldn't be stored as one string with the domain attached.
This is because the handle should be portable and not tied to a specific domain.
Of course the current domain for a handle should be stored, just separately and only used when needed. (For example looking up the public split key)

A username with an attached domain has the following format:
`[handle]@[domain]`

Example: `beray_drubs_pant57107@fis.rocc.systems`

NOTE: When sending a signed request with only your handle, it is advised to attach the domain, if there's a chance the Server doesn't know it yet.
If the server cannot resolve your handle, it will throw an error and ideally your client would send the handle with the domain attached.

NOTE: The domain technically allows to have the port defined too, useful for testing with localhost.


## Cryptographic Handle Derivation
(TODO)


See [Example Implementation](src/main/java/com/masl/goofy_protocol_core/crypto/connected/HandleCrypto.java) for a working example of the Handle Derivation.



## Strength
(TODO)
```
// Strength of handles
// c = 2 -> ~44 bit (15000^2 * 10^5 = 2.3e13 combinations)
// c = 3 -> ~58 bit (15000^3 * 10^5 = 3.4e17 combinations)
// c = 4 -> ~72 bit (15000^4 * 10^5 = 5.1e21 combinations)
```


# Signed Requests
(TODO)

## Parts
(TODO)

## Headers
The following headers are needed for Signed Requests:
* `X-Goofy-Public-Key`: The Public Split Key of the Sender (format defined above)
* `X-Goofy-Handle`: The Handle of the Sender (format defined above, can have a domain attached)
* `X-Goofy-Signature`: The Signature of the Request (format defined above)
* `X-Goofy-Id`: A random Id in the form of a Long (64bit) Integer, used to prevent replay attacks (The server won't store them forever usually)
* `X-Goofy-Valid-Until`: A timestamp in the form of a Long (64bit) Integer representing the time in milliseconds since epoch, used to enforce a time limit on the validity of the request

The default validity of a Signed Request should be 60 seconds. This is because surprisingly a lot of devices aren't closely synchronized with the actual time and can be off by some time. (Sometimes even multiple minutes)

## Signature
(TODO)

Servers should reject requests with a valid until timestamp, which has already passed or is too far in the future (for example >1h).

## Validity
(TODO)


## Signature Sizes
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