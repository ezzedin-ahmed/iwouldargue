package org.iwouldargue.config;

import com.typesafe.config.ConfigException;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.iwouldargue.FileService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "files")
@Data
@Validated
public class FilesConfig {
    @NotNull
    private String savesDir;

    @Bean
    public FileService fileService() throws IOException {
        var savesDir = Paths.get(this.savesDir);
        if (!savesDir.isAbsolute()) {
            throw new ConfigException.BadBean("files.savesDir should be absolute path");
        }
        return new FileService(savesDir);
    }
}
