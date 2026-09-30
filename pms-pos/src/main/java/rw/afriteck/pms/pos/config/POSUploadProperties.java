package rw.afriteck.pms.pos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pms.pos.upload")
@Getter
@Setter
public class POSUploadProperties {

    private String productImageDir;
    private long maxFileSizeBytes = 2_097_152; // 2MB
}
