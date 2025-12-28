package com.worktreewise.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.worktreewise.idea.WorktreeWiseLocator;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OpenInWorktreeWiseAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        String projectPath = project.getBasePath();
        if (projectPath == null) return;

        try {
            Path exe = WorktreeWiseLocator.findExecutable();

            List<String> cmd = new ArrayList<>();
            cmd.add(exe.toString());
            cmd.add("--open");
            cmd.add(projectPath);

            new ProcessBuilder(cmd).start();

        } catch (Exception ex) {
            Messages.showErrorDialog(
                    project,
                    ex.getMessage(),
                    "WorktreeWise"
            );
        }
    }
}
