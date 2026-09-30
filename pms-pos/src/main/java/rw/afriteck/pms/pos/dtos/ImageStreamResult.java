package rw.afriteck.pms.pos.dtos;

import org.springframework.core.io.Resource;

public record ImageStreamResult (
        Resource resource,
        String contentType
) {}
