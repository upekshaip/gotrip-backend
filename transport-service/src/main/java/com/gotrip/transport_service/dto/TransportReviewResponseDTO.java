package com.gotrip.transport_service.dto;

import com.gotrip.common_library.dto.user.TravellerContactInfo;
import com.gotrip.transport_service.model.TransportReview;

public record TransportReviewResponseDTO(
        TransportReview review,
        TravellerContactInfo travellerContact
) {}