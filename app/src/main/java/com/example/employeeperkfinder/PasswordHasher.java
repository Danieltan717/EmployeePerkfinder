package com.example.employeeperkfinder;

//import org.mindrot.jbcrypt.BCrypt;
//
//public class PasswordHasher {
//
//    // Generates a BCrypt salt
//    public static String generateSalt() {
//        return BCrypt.gensalt();
//    }
//
//    // Hashes the password using BCrypt with the provided salt
//    public static String hashPassword(String password) {
//        return BCrypt.hashpw(password, generateSalt());
//    }
//
//    // Verifies if the input password matches the stored BCrypt hash
//    public static boolean verifyPassword(String password, String storedHash) {
//        return BCrypt.checkpw(password, storedHash);
//    }
//}

          //V2.0
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class PasswordHasher {

    // Generates a unique salt using UUID
    public static String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // Hashes the password with SHA-512 using the provided salt
    public static String hashPassword(String salt, String password) {
        String saltedPassword = salt + password;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : hashedBytes)
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found!");
        }
    }

    // Verifies if the input password (with salt) matches the stored hash
    public static boolean verifyPassword(String salt, String password, String storedHash) {
        String hashToVerify = hashPassword(salt, password);
        return hashToVerify.equals(storedHash);
    }
}


// V1.0
//import org.mindrot.jbcrypt.BCrypt;


//public class PasswordHasher {
//    public static String hashPassword(String plainPassword){
//        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
//    }
//
//    public static boolean checkPassword(String plainPassword, String hashedPassword){
//        return BCrypt.checkpw(plainPassword, hashedPassword);
//    }
//}
