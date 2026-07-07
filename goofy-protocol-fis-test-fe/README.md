# Goofy Protocol FIS (Federated Identity Server) TEST Frontend

WIP "Reference" Implementation of a FIS for Goofy Protocol.

**NOTE**: this is mostly for testing currently, do not base anything on this!

**NOTE**: I'm using AI Tools to try and translate my Java Library code, the code wont be perfect and it might contain issues. 
There is a good chance i will fully rewrite it in the future, but for now testing it should hopefully be okay.
It is also a good showcase if it's even compatible and if there are any Java / JS Compatibility issues i need to address first!

## Planned Register Flow
* User goes to the Register Page
* Ideally User already has a Register Code or Requests one
* User enters the Code to Check
* User then generates a keypair (or imports one) + sees the handle
  * The user is presented with the supported keypair types and can select one
  * Of course the user can regenerate it until the handle is good
* Then the User will be prompted to save it (e.g. download it)
* Then on the next step the user will be forced to import the keypair, to verify they have it
* If successful, the user is registered
* Before sending the user to the homepage, the User will be asked if they want to set up a username/password login.
  * If they do, they will be prompted to enter a username and password, which will be used to store/retrieve the keypair stored on the server (encrpyted with the password)
* The user will be asked if they want the keypair to be saved in LocalStorage (+ encrypted with a password?) or SessionStorage when using the app.

The login and storage should always be able to be changed by the user.



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