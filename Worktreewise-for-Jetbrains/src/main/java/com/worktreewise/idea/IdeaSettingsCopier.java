package com.worktreewise.idea;

import java.io.IOException;
import java.nio.file.*;

public class IdeaSettingsCopier {

    public static void copyAll(Path sourceProject, Path targetProject) throws IOException {
        Path sourceIdea = sourceProject.resolve(".idea");
        Path targetIdea = targetProject.resolve(".idea");

        if (!Files.exists(sourceIdea)) return;

        Files.walk(sourceIdea).forEach(path -> {
            try {
                Path relative = sourceIdea.relativize(path);
                Path target = targetIdea.resolve(relative);

                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
