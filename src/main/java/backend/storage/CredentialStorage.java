package backend.storage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CredentialStorage {
    public static class SavedCredentials {
        private final String email;
        private final String password;

        public SavedCredentials(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }

    private static final String ALGO = "AES";
    private static final String KEY_STR = "MemoraKey1234567";

    private static File getFile() {
        String home = System.getProperty("user.home");
        File dir = new File(home, ".memora");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, "credentials.dat");
    }

    private static SecretKeySpec getKey() {
        byte[] key = new byte[16];
        byte[] original = KEY_STR.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(original, 0, key, 0, Math.min(original.length, key.length));
        return new SecretKeySpec(key, ALGO);
    }

    public static void save(String email, String password) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            String data = email + "\n" + password;
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            Files.write(getFile().toPath(), Base64.getEncoder().encode(encrypted));
        } catch (Exception e) {
            // Ignore storage errors
        }
    }

    public static SavedCredentials load() {
        try {
            File file = getFile();
            if (!file.exists()) {
                return null;
            }
            byte[] encoded = Files.readAllBytes(file.toPath());
            byte[] encrypted = Base64.getDecoder().decode(encoded);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            String decrypted = new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
            String[] lines = decrypted.split("\n", 2);
            if (lines.length == 2) {
                return new SavedCredentials(lines[0], lines[1]);
            }
        } catch (Exception e) {
            // Ignore storage errors
        }
        return null;
    }

    public static void clear() {
        try {
            File file = getFile();
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            // Ignore storage errors
        }
    }
}
