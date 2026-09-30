package rw.afriteck.pms.pos.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import rw.afriteck.pms.common.exception.BusinessRuleViolationException;
import rw.afriteck.pms.pos.config.POSUploadProperties;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductImageStorage {


    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    private final POSUploadProperties uploadProperties;

    public String store(UUID productId, MultipartFile file) {
        validate(file);

        String extension = extensionFor(file.getContentType());
        String filename = productId + "-" + UUID.randomUUID() + extension;

        Path targetDir = Path.of(uploadProperties.getProductImageDir());
        Path targetPath = targetDir.resolve(filename);

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store product image for product " + productId, e);
        }

        return targetPath.toString();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleViolationException("EMPTY_FILE", "No image file provided");
        }
        if (file.getSize() > uploadProperties.getMaxFileSizeBytes()) {
            throw new BusinessRuleViolationException("FILE_TOO_LARGE",
                    "Image exceeds maximum size of " + uploadProperties.getMaxFileSizeBytes() + " bytes");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessRuleViolationException("UNSUPPORTED_FILE_TYPE",
                    "Unsupported image type: " + file.getContentType());
        }
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new BusinessRuleViolationException("UNSUPPORTED_FILE_TYPE",
                    "Unsupported image type: " + contentType);
        };
    }
}
