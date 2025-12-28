package com.worktreewise.idea;

import com.intellij.openapi.util.SystemInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorktreeWiseLocator {

    public static Path findExecutable() throws IOException {
        Path configPath = getConfigPath();

        if (!Files.exists(configPath)) {
            throw new IOException("WorktreeWise is not installed or not launched yet.");
        }

        String json = Files.readString(configPath);
        String marker = "\"executable\"";

        int i = json.indexOf(marker);
        if (i == -1) throw new IOException("Invalid WorktreeWise config");

        int start = json.indexOf('"', i + marker.length());
        int end = json.indexOf('"', start + 1);

        return Paths.get(json.substring(start + 1, end));
    }

    private static Path getConfigPath() {
        if (SystemInfo.isWindows) {
            return Paths.get(
                    System.getenv("APPDATA"),
                    "WorktreeWise",
                    "worktreewise.json"
            );
        }

        if (SystemInfo.isMac) {
            return Paths.get(
                    System.getProperty("user.home"),
                    "Library/Application Support/WorktreeWise/worktreewise.json"
            );
        }

        return Paths.get(
                System.getProperty("user.home"),
                ".config/WorktreeWise/worktreewise.json"
        );
    }
}
