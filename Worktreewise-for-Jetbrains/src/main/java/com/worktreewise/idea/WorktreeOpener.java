package com.worktreewise.idea;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.File;
import java.nio.file.Path;

public class WorktreeOpener {

    public static void open(Project currentProject, Path path) {
        File dir = path.toFile();
        if (!dir.exists()) return;

        // Pre-refresh
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dir);

        OpenProjectTask task = new OpenProjectTask(
                true,   // open in new window
                null,
                false,
                false
        );

        Project opened = ProjectManagerEx.getInstanceEx()
                                         .openProject(path, task);

        if (opened != null) {
            VirtualFileManager.getInstance().syncRefresh();
        }
    }
}
