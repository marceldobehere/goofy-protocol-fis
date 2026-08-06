package com.masl.goofy_protocol_fis_be.rest;

import com.masl.goofy_protocol_fis_be.dto.request.ServicePublicDataUpdateDto;
import com.masl.goofy_protocol_fis_be.entity.IdentityStorageEntry;
import com.masl.goofy_protocol_fis_be.entity.User;
import com.masl.goofy_protocol_fis_be.exception.base.swagger.FisEndpoint;
import com.masl.goofy_protocol_fis_be.exception.client.IdentityEntryNotFound;
import com.masl.goofy_protocol_fis_be.properties.GeneralProperties;
import com.masl.goofy_protocol_fis_be.repository.IdentityStorageEntryRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/fis-api/redirect")
@Tag(name = "Redirects", description = "Endpoints to get Frontend URL Redirects for Service Login/Config/Access")
public class RedirectEndpoint {
    private final IdentityStorageEntryRepository identityStorageEntryRepository;
    private final GeneralProperties generalProperties;
    private final ObjectMapper mapper = new ObjectMapper();

    public RedirectEndpoint(IdentityStorageEntryRepository identityStorageEntryRepository, GeneralProperties generalProperties) {
        this.identityStorageEntryRepository = identityStorageEntryRepository;
        this.generalProperties = generalProperties;
    }

    @PutMapping("/update-public-identity-entry/{handle}")
    @FisEndpoint(summary = "Gets a Link to the Frontend Page to update the Public Service Data of an Identity")
    public String updatePublicIdentityEntryData(@PathVariable String handle, @Valid @RequestBody ServicePublicDataUpdateDto updateData) throws IdentityEntryNotFound {
        // Find Entry
        IdentityStorageEntry entry = identityStorageEntryRepository.findByHandle(handle);
        if (entry == null)
            throw new IdentityEntryNotFound(handle);

        // Get Base Frontend URL
        User user = entry.getCreatedBy();
        String frontendUrl = user.getCustomFrontendUrl() != null ? user.getCustomFrontendUrl() : generalProperties.getFrontendUrl();

        // Build Redirect URI
        return UriComponentsBuilder
                .fromUriString(frontendUrl + "/user/service-entry-list/")
                .queryParam("setPublicServiceEntry", URLEncoder.encode(mapper.writeValueAsString(updateData), StandardCharsets.UTF_8))
                .fragment(handle)
                .build(false)
                .toUriString();
    }



    // TODO: Think about Fis Clients and if they need federation too / what to do with the redirects then? Maybe standardize and let user save their frontend url or just say frontend is set by backend?

    // TODO: Look into how Services would request a user to create a service entry + tables with access + bucket access?

    // TODO: Implement Backend Override similar to RootEndpoint#index + Use the custom Frontend URL defined by the User

    /*
    ### External Service Access
    These Endpoints get used by services.

    Basically for a user there are two things:
    * Services connected to a identity (Used for login)
        * Basically just a list of services and identities
        * Useful so you can "autofill" the "login" credentials for a service
    * Service Data Access, so giving a service permission to create/read/write tables and buckets. Each Service Data Access will have the service and the handle and then a local scope name,

    -> Login/Config/Access

    #### Get Service name


    #### (REDIRECT) Add Service Access to Handle
    This should be a URL that services can link users to which should redirect users to a (ideally) Frontend Page of the FIS where a logged in user can add the service (defined in the query parameter) to a handle.

    This is there to make the userflow easier.

    This can be used to setup Service Entry / Basic Data Access with Tables / Buckets but also without, just to have the service be "connected" with the user/identity so that it can be used to login.


    #### (REDIRECT) Get Service Login Credentials / Keypair
    This should be a URL that services can link users to which should redirect users to a (ideally) Frontend Page of the FIS where a logged in user can get all handles associated with the service (defined in the query parameter) and can get the keypair which can be used to log in.

    It could potentially add a link to the service which would get the credentials in the url (ideally encoded inside the fragment `#` portion for security reasons) so that the user can get signed in by just selecting the handle and getting redirected to the specific service url.

    This is there to make the userflow easier.
    */
}
