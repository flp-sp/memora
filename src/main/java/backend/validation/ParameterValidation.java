package backend.validation;

import java.util.regex.Pattern;

public class ParameterValidation {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{1,}$");

    
    public static boolean validateEmail(String email){
        if (email != null && EMAIL_PATTERN.matcher(email.trim()).matches()){
            return true;
        }
        else{
            return false;
        }
    }


    public static boolean validatePass(String pass){
        if (pass.length() < 6){
            return false;
        }
        else{
            return true;
        }
    }
}
