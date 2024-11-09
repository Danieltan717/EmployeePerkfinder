package com.example.employeeperkfinder;

public class HelperClass {

    private String email, passwordHash, salt, employee_id;

//    // Constructor for setting up user data with hashed password and salt
//    public HelperClass(String email, String passwordHash, String salt, String employee_id) {
//        this.email = email;
//        this.passwordHash = passwordHash;
//        this.salt = salt;
//        this.employee_id = employee_id;
//    }

    // Default constructor for Firebase
//    public HelperClass() {
//    }

    // Getter and setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getter and setter for passwordHash
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Getter and setter for salt
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    // Getter and setter for employee_id
    public String getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public HelperClass(String email, String passwordHash, String salt, String employee_id){
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.employee_id = employee_id;
    }


    public HelperClass(){

    }
}





//package com.example.employeeperkfinder;
//
//public class HelperClass {
//
//    private String email,password,employee_id;
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public String getEmployee_id() {
//        return employee_id;
//    }
//
//    public void setEmployee_id(String employee_id) {
//        this.employee_id = employee_id;
//    }
//
//    public HelperClass(String email, String password, String employee_id) {
//        this.email = email;
//        this.password = password;
//        this.employee_id = employee_id;
//    }
//
//    public HelperClass() {
//    }
//}
