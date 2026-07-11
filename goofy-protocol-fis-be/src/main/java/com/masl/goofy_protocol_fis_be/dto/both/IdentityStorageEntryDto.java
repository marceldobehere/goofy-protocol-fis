package com.masl.goofy_protocol_fis_be.dto.both;

import com.masl.goofy_protocol_core.crypto.connected.HandleCrypto;
import com.masl.goofy_protocol_core.crypto.isolated.asymm.GlobAsymmCrypto;
import com.masl.goofy_protocol_fis_be.entity.FieldSize;
import com.masl.goofy_protocol_fis_be.exception.client.InvalidPublicKey;
import com.masl.goofy_protocol_fis_be.exception.client.InvalidSignedObject;
import com.masl.goofy_protocol_fis_be.exception.client.NotMatchingPublicKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityStorageEntryDto {
    @NotBlank
    @Size(max = FieldSize.HANDLE_LEN)
    private String handle;

    @NotNull
    @Size(max = FieldSize.TITLE_LEN)
    private String name;

    @NotBlank
    @Size(max = FieldSize.PUB_KEY_LEN)
    private String pubSplitKey;

    @NotBlank
    @Size(max = FieldSize.FULL_KEY_LEN)
    private String encKeypairEntry;

    @NotBlank
    @Size(max = FieldSize.SIGNATURE_LEN)
    private String encKeypairEntrySignature;

    public static void checkValidity(IdentityStorageEntryDto entry, HandleCrypto handleCrypto, GlobAsymmCrypto asymmCrypto) throws InvalidSignedObject, InvalidPublicKey, NotMatchingPublicKey {
        if (!asymmCrypto.checkPublicSplitKey(entry.getPubSplitKey()))
            throw new InvalidPublicKey();
        if (!handleCrypto.verifyKeyAndHandle(entry.getPubSplitKey(), entry.getHandle()))
            throw new NotMatchingPublicKey(entry.getHandle(), entry.getPubSplitKey());
        if (!asymmCrypto.verifyStr(entry.getEncKeypairEntry(), entry.getEncKeypairEntrySignature(), entry.getPubSplitKey()))
            throw new InvalidSignedObject();
    }
}
