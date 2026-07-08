package com.masl.goofy_protocol_fis_be.rest.storage;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/identity-storage")
@Tag(name = "Identity Storage", description = "Endpoints relating to Identity Keypair Storage for Services.")
public class IdentityStorageEndpoint {
    /*
    ### Encrypted (Service) Identity Storage
    Endpoints for storing and retrieving generated Identities. They will be stored encrypted. Of course only used by registered users.

    The Entries are symmetrically encrypted with a key.
    This key is stored alongside the entry but it is asymmetrically encrypted with the public key of the base identity, meaning only the user can decrypt/use them.

    The identity entries should have the handle and a custom name added to them which doesnt need to be encrypted and a string for notes which should be encrypted.

    The Entries should have a normal size constraint, maybe max 100KB. There should also be a maximum number of encrypted identities, but that depends on the FIS, I would say at minimum 20 should be good.

    #### Get All Identities
    This will return a list of all identities. Will only have custom names and notes and maybe handles.

    #### Get Specific Identity
    This will return all data for a single identity.
    The data should be used to be able to export the identity.

    #### Create/Update Identity
    This will allow users to create/update identity entries for a custom name.

    You should be able to import an identity, ideally with all the data of the `Get Specific Identity` Endpoint.

    #### Delete Identity
    This will delete an identity entry for a custom name.
    */
}
