package com.worktreewise.notifications;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;

public class LicenceNotifications {

    private static final String MARKETPLACE_URL = "https://www.worktreewise.com/?utm_source=worktreewise-for-jetbrains";

    public static final String GROUP_ID = "WorktreeWise Licensing";

    private LicenceNotifications() {}

    public static void showLicenseRequired() {

        NotificationGroup group =
                NotificationGroupManager.getInstance()
                                        .getNotificationGroup(GROUP_ID);

        Notification notification = group.createNotification(
                "WorktreeWise License Required",
                "This feature requires an active WorktreeWise license.",
                NotificationType.WARNING
        );

        notification.addAction(
                NotificationAction.createSimple("Upgrade to Pro", () ->
                        BrowserUtil.browse(MARKETPLACE_URL)
                )
        );

        notification.notify(null);
    }


}
