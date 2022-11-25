public class substitutionCipher {

    static char[] small = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    static char[] capital = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    /**
     * Encrypts a message using the substitution cipher.
     *
     * @param message The message to encrypt.
     * @param key     The key to use for encryption.
     * @return The encrypted message.
     */
    static String encrypt(String message, int key) {
        StringBuilder result = new StringBuilder(message);
        for (int i = 0; i < result.length(); i++) {
            for (int j = 0; j < 26; j++) {
                if (result.charAt(i) == small[j]) {
                    result.setCharAt(i, small[(j + key) % 26]);
                    break;
                } else if (message.charAt(i) == capital[j]) {
                    result.setCharAt(i, capital[(j + key) % 26]);
                    break;
                }
            }
        }

        return result.toString();
    }

    /**
     * Decrypts a message using the substitution cipher.
     *
     * @param message The message to decrypt.
     * @param key     The key to use for decryption.
     * @return The decrypted message.
     */
    static String decrypt(String message, int key) {
        StringBuilder result = new StringBuilder(message);
        for (int i = 0; i < result.length(); i++) {
            for (int j = 0; j < 26; j++) {
                if (result.charAt(i) == small[j]) {
                    int index = (j - key) % 26;
                    if (index < 0) {
                        index += 26;
                    }
                    result.setCharAt(i, small[index]);
                    break;
                } else if (message.charAt(i) == capital[j]) {
                    int index = (j - key) % 26;
                    if (index < 0) {
                        index += 26;
                    }
                    result.setCharAt(i, capital[index]);
                    break;
                }
            }
        }

        return result.toString();
    }
}
