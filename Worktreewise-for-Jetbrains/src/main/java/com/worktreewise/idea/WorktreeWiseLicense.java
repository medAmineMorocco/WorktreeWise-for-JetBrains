package com.worktreewise.idea;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.PathManager;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


import org.json.JSONObject;

public final class WorktreeWiseLicense {

    private static final int TRIAL_PERIOD_DAYS = 5;

    // AES config (same as Electron)
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte[] KEY =
            "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    private static final String FILE_WHERE_TO_STORE_SUBSCRIPTION =
            ".sys_cache_75h4kF.tmp";

    private static final Path FILE_1 =
            Path.of(System.getProperty("user.home"),
                    FILE_WHERE_TO_STORE_SUBSCRIPTION);

    private static final Path FILE_2 =
            Path.of(PathManager.getSystemPath(), ".cache_store.tmp");

    private WorktreeWiseLicense() {}

    // 🔑 Public API
    public static boolean isLicensed() {
        try {
            JSONObject packInfos = loadPackInfos();
            if (packInfos == null) {
                return false;
            }

            String pack = packInfos.optString("pack", "");
            String version = packInfos.optString("version", "");

            // ✅ Paid subscription
            if (!"Free Trial".equals(pack)) {
                return true;
            }

            // ⏳ Trial
            String startTrialDate = packInfos.optString("startTrialDate", null);
            if (startTrialDate == null) {
                return false;
            }

            LocalDate start = LocalDate.parse(startTrialDate);
            long daysUsed = ChronoUnit.DAYS.between(start, LocalDate.now());

            return daysUsed <= TRIAL_PERIOD_DAYS;

        } catch (Exception e) {
            return false;
        }
    }

    // =========================
    // 🔍 Load + decrypt logic
    // =========================

    private static JSONObject loadPackInfos() {
        JSONObject data = loadAndDecrypt(FILE_1);
        if (data == null) {
            data = loadAndDecrypt(FILE_2);
        }
        return data;
    }

    private static JSONObject loadAndDecrypt(Path path) {
        try {
            if (!Files.exists(path)) {
                return null;
            }

            String encryptedJson = Files.readString(path);
            JSONObject encrypted = new JSONObject(encryptedJson);

            String encryptedData = encrypted.getString("encryptedData");
            String ivHex = encrypted.getString("iv");

            String decrypted = decrypt(encryptedData, ivHex);
            return new JSONObject(decrypted);

        } catch (Exception e) {
            return null;
        }
    }

    // =========================
    // 🔐 AES decryption
    // =========================

    private static String decrypt(String encryptedHex, String ivHex)
            throws Exception {

        byte[] ivBytes = hexToBytes(ivHex);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decrypted =
                cipher.doFinal(hexToBytes(encryptedHex));

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)
                    ((Character.digit(hex.charAt(i), 16) << 4)
                            + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

}
