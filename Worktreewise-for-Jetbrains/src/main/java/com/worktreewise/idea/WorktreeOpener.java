package com.worktreewise.idea;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WorktreeOpener {

    public static void open(Project currentProject, Path path) {
        File dir = path.toFile();
        if (!dir.exists()) return;

        // Pre-refresh
        LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dir);


        Path target = path.toAbsolutePath().normalize();

        for (Project openProject : ProjectManagerEx.getInstanceEx().getOpenProjects()) {
            String basePath = openProject.getBasePath();
            if (basePath == null) continue;

            try {
                Path open = Path.of(basePath).toAbsolutePath().normalize();

                if (Files.isSameFile(target, open)) {
                    focusProject(openProject);
                    return;
                }
            } catch (IOException ignored) {
                // fallback if filesystem comparison fails
                if (target.toString().replace('\\', '/')
                          .equalsIgnoreCase(basePath.replace('\\', '/'))) {
                    focusProject(openProject);
                    return;
                }
            }
        }


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

    private static void focusProject(Project project) {
        IdeFrame frame = WindowManager.getInstance().getIdeFrame(project);
        if (frame == null) return;

        Component c = frame.getComponent();
        if (c instanceof JFrame jFrame) {
            jFrame.setState(Frame.NORMAL);
            jFrame.toFront();
            jFrame.requestFocus();
        } else {
            c.requestFocus();
        }
    }


}
