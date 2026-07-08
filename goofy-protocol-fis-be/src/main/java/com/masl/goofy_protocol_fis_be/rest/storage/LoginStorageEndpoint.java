package com.masl.goofy_protocol_fis_be.rest.storage;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login-storage")
@Tag(name = "Login Storage", description = "Endpoints relating to Logging in using a username/pasword and retrieving the encrypted Keypair.")
public class LoginStorageEndpoint {
    /*
    ### Encrypted Password/Keypair Storage (Base Identity / Account)
    Endpoints for storing encrypted keypairs, which are used for password based login.

    Each registered user can have one entry with a chosen username (if available) and can store text data in it. The entry can be looked up publically with the username as the key.

    The username will never be sent in plaintext, it will be hashed beforehand so that the FIS does not know the username and only the user knows it. (As an additional layer of security)

    It's intended for users to symmetrically encrypt their keypair with a password and store that in this storage. (If want to store it conveniently)

    The Entry should have a normal size constraint, maybe max 100KB.

    #### Get Storage Entry for Username
    Gets the Storage entry for a username or a 404 if it doesnt exist


    #### Post Storage Entry for Username
    Sets the Storage entry for a username, if the username is free.
    Request needs to be signed and a user can only have one username at once, if its a different one, then the old one will be deleted.


    #### Delete Storage Entry for Username
    Request needs to be signed.
    */
}
