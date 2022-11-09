public class vigenereCipher {
    static String generateKey(String str, String key) {
        int x = str.length();
        StringBuilder keyBuilder = new StringBuilder(key);
        for (int i = 0; ; i++) {
            if (x == i) {
                i = 0;
            }
            if (keyBuilder.length() == str.length()) {
                break;
            }
            keyBuilder.append(keyBuilder.charAt(i));
        }
        key = keyBuilder.toString();
        return key;
    }

    static String cipherText(String str, String key) {
        StringBuilder cipherText = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            // converting in range 0-25
            int x = (str.charAt(i) + key.charAt(i)) % 26;
            // convert into alphabets(ASCII)
            x += 'A';
            cipherText.append((char) (x));
        }
        return cipherText.toString();
    }

    static String originalText(String cipher_text, String key) {
        StringBuilder originalText = new StringBuilder();
        for (int i = 0; i < cipher_text.length() && i < key.length(); i++) {
            // converting in range 0-25
            int x = (cipher_text.charAt(i) - key.charAt(i) + 26) % 26;
            // convert into alphabets(ASCII)
            x += 'A';
            originalText.append((char) (x));
        }
        return originalText.toString();
    }
}
