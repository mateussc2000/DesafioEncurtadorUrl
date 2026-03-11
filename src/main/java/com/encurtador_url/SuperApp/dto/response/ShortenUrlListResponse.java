package com.encurtador_url.SuperApp.dto.response;

import java.util.List;

public record ShortenUrlListResponse(
        List<ShortenUrlResponse> shortenUrlList,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
