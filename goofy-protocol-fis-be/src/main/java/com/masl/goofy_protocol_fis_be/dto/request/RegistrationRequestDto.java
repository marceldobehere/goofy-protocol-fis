package com.masl.goofy_protocol_fis_be.dto.request;

import com.masl.goofy_protocol_fis_be.entity.FieldSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDto {
    @NotBlank
    @Size(max = FieldSize.NORMAL_TEXT_LEN)
    private String message;

    @NotBlank
    @Size(max = FieldSize.SHORT_TEXT_LEN)
    private String contact;

    private String optEmail;
}
