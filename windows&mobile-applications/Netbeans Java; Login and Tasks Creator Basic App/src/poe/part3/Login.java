/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package poe.part3;

/**
 *
 * @author lab_services_student
 */
import javax.swing.JOptionPane;

public class Login {
    private String username;
    private String password;
    private String firstName;
    private String lastName;

    public Login() {
    }
    
    

    public Login(String username, String password, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstname(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

   

  
    public boolean checkUserName(){
        boolean correct = false ;
        int length = username.length();
        int pos = username.indexOf("_");
        for (int x = 0; x< length;x++){
        if (length <= 5 && pos != -1){
            correct = true;
        
                
            } 
        }  
        return correct;
    }
    
    public boolean checkPasswordComplexity(){
        boolean correct = false;
        int length = password.length();
        if(password.matches(".*[0-9].*") && password.matches(".*[A-Z].*") && password.matches(".*[!-~].*") && length >= 8){
        correct = true;
        }
        return correct;
    }
    
   public String registerUser(){
        String z;
        if(checkUserName() == true && checkPasswordComplexity() == true){
            z = "Username successfully captured"
                    + "\n" + "Password successfully captured";
            }
            else{
            z = "Username is not correctly formatted, please ensure that your "
                    + "username contains and is no more than 5 characters in "
                    + "length\n or Password is not correctly"
                    + " formatted please ensure that the password contains at least 8 "
                    + "characters, a capital letter, a number and a special character.";
           
        }
        return z;
    }
    
    public boolean loginUser(){
        boolean correct = false;
        JOptionPane.showMessageDialog(null, "-----Login with the same information----");
        String newUsername = JOptionPane.showInputDialog(null, "Enter correct username");
        String newPassword = JOptionPane.showInputDialog(null, "Enter correct password");
        if(username.equals(newUsername) && password.equals(newPassword)){
        correct = true;
            
        }

       return correct;
    }
    public String returnLoginStatus(){
        String z ;
        if(loginUser() == true){
         z ="Welcome " + firstName + "," + lastName + " It is great to see you again";      
        }
        else {
            z = "Username or password incorrect, please try again";
        }
        return z ;
    }
  
}

