package com.masl.goofy_protocol_fis_be.dto.both;

import com.masl.goofy_protocol_fis_be.entity.FieldSize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntryDto {
    @NotNull
    @Size(max = FieldSize.SHORT_TEXT_LEN)
    private String name; // A name for the Service Entry, should ideally be unique but doesn't have to be

    private String usedService; // The URL or Name of the Service, should be helpful but is optional

    // Will only be set by the FIS
    private String uuid;
}
