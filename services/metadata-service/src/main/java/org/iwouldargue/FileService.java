package org.iwouldargue;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public class FileService {

    private final Path savesDir;

    public FileService(Path savesDir) throws IOException {
        if (Files.notExists(savesDir)) {
            Files.createDirectory(savesDir);
        }
        this.savesDir = savesDir;
    }

    public Optional<Path> getFilePath(UUID fileId) {
        var filepath = filePath(fileId);
        if (filepath.toFile().exists()) {
            return Optional.of(filepath);
        } else {
            return Optional.empty();
        }
    }

    public UUID write(MultipartFile file) throws IOException {
        var fileId = UUID.randomUUID();
        file.transferTo(filePath(fileId).toFile());
        return fileId;
    }

    private Path filePath(UUID fileId) {
        return savesDir.resolve(fileId.toString());
    }
}
