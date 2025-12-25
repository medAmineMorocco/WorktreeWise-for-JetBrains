package com.worktreewise.services;

import com.worktreewise.model.WorktreeInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GitWorktreeService {

    public static List<WorktreeInfo> listWorktrees(Path projectRoot) {
        List<WorktreeInfo> result = new ArrayList<>();

        try {
            Process process = new ProcessBuilder(
                    "git", "worktree", "list", "--porcelain"
            ).directory(projectRoot.toFile()).start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            String path = null;
            String branch = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("worktree ")) {
                    path = line.substring(9);
                } else if (line.startsWith("branch ")) {
                    branch = line.substring(7);
                } else if (line.isEmpty() && path != null) {
                    result.add(new WorktreeInfo(path, branch));
                    path = null;
                    branch = null;
                }
            }

        } catch (Exception ignored) {}

        return result;
    }
}
