package com.gotrip.transport_service.dto;

import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.transport_service.model.Transport;

public record TransportResponseDTO(
        Transport transport,
        TravellerContactInfo providerContact
) {}