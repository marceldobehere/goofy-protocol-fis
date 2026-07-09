# Goofy Protocol FIS (Federated Identity Server) TEST Frontend

WIP "Reference" Implementation of a FIS for Goofy Protocol using NextJS.

**NOTE**: this is mostly for testing currently, do not base anything on this!

**NOTE**: I'm using AI Tools to try and translate my Java Library code, the code wont be perfect and it might contain issues.
There is a good chance i will fully rewrite it in the future, but for now testing it should hopefully be okay.
It is also a good showcase if it's even compatible and if there are any Java / JS Compatibility issues i need to address first!


## Get Started
* Clone the repo
* Install dependencies with `npm install`
* Run the tests with `npm run test`
* Run the dev server with `npm run dev`
* Profit?



## Other Stuff


## Current Support

### Symmetric Cryptography
Supported Types:
* AES-128-GCM
* AES-196-GCM
* AES-256-GCM
* ChaCha20

### Asymmetric Cryptography
Supported Types:
* RSA 2048
* RSA 3072
* RSA 4096
* EC_C25519

Not (yet) Supported Types:
* EC_P256
* EC_P384
* ML-KEM (512) + ML-DSA (44)
* ML-KEM (768) + ML-DSA (65)
* ML-KEM (1024) + ML-DSA (87)

### User Handle Derivation
Is supported

### Signed Requests
Is supported