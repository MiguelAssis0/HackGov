package com.fiap.hackgov.bidding.internal.DTOs.etp;

import java.util.UUID;

public record ETPDTO(

        UUID id, UUID requisitionId, String content

) {
}