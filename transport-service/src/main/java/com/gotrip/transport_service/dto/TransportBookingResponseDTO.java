package com.gotrip.transport_service.dto;

import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.transport_service.model.TransportBooking;

public record TransportBookingResponseDTO(
        TransportBooking booking,
        TravellerContactInfo travellerContact,
        TravellerContactInfo providerContact
) {}