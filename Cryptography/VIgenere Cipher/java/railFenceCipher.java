public class railFenceCipher {

    /**
     * Encrypts a message using the rail fence cipher.
     * @param message The message to encrypt.
     * @param key The key to use for encryption.
     * @return The encrypted message.
     */
    static String encrypt(String message, int key) {
        // Create a 2D array to store the message.
        char[][] rail = new char[key][message.length()];

        // Fill the array with the message.
        for (int i = 0; i < key; i++) {
            for (int j = 0; j < message.length(); j++) {
                rail[i][j] = '\n';
            }
        }
        boolean dir_down = false;
        int row = 0, col = 0;
        for (int i = 0; i < message.length(); i++) {
            if (row == 0 || row == key - 1) {
                dir_down = !dir_down;
            }
            rail[row][col++] = message.charAt(i);
            if (dir_down) {
                row++;
            } else {
                row--;
            }
        }

        // Read the array and create the encrypted message.
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < key; i++) {
            for (int j = 0; j < message.length(); j++) {
                if (rail[i][j] != '\n') {
                    result.append(rail[i][j]);
                }
            }
        }
        return result.toString();
    }

    /**
     * Decrypts a message using the rail fence cipher.
     * @param message The message to decrypt.
     * @param key The key to use for decryption.
     * @return The decrypted message.
     */
    static String decrypt(String message, int key) {
        // Create a 2D array to store the message.
        char[][] rail = new char[key][message.length()];

        // Fill the array with the message.
        for (int i = 0; i < key; i++) {
            for (int j = 0; j < message.length(); j++) {
                rail[i][j] = '\n';
            }
        }
        boolean dir_down = false;
        int row = 0, col = 0;
        for (int i = 0; i < message.length(); i++) {
            if (row == 0 || row == key - 1) {
                dir_down = !dir_down;
            }
            rail[row][col++] = '*';
            if (dir_down) {
                row++;
            } else {
                row--;
            }
        }
        int index = 0;
        for (int i = 0; i < key; i++) {
            for (int j = 0; j < message.length(); j++) {
                if (rail[i][j] == '*' && index < message.length()) {
                    rail[i][j] = message.charAt(index++);
                }
            }
        }
        // Read the array and create the decrypted message.
        StringBuilder result = new StringBuilder();
        row = 0;
        col = 0;
        for (int i = 0; i < message.length(); i++) {
            if (row == 0) {
                dir_down = true;
            }
            if (row == key - 1) {
                dir_down = false;
            }
            if (rail[row][col] != '*') {
                result.append(rail[row][col++]);
            }
            if (dir_down) {
                row++;
            } else {
                row--;
            }
        }
        return result.toString();
    }
}
