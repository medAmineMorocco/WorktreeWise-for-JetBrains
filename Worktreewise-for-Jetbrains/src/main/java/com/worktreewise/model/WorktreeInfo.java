package com.worktreewise.model;

public class WorktreeInfo {
    private final String path;
    private final String branch;

    public WorktreeInfo(String path, String branch) {
        this.path = path;
        this.branch = branch;
    }

    public String getPath() {
        return path;
    }

    public String getBranch() {
        return branch;
    }
}
