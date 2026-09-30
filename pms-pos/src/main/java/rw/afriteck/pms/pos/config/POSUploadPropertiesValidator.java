package rw.afriteck.pms.pos.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class POSUploadPropertiesValidator {

    private final POSUploadProperties uploadProperties;

    @PostConstruct
    public void validate() {
        if (uploadProperties.getProductImageDir() == null || uploadProperties.getProductImageDir().isBlank()) {
            throw new IllegalStateException("pms.pos.upload.product-image-dir must be set in application.yml");
        }
    }
}
