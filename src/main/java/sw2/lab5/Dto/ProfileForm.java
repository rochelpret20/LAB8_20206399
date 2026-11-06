package sw2.lab5.Dto;
import javax.validation.constraints.*;
public class ProfileForm {
 @NotBlank @Size(max=50) private String firstname;
 @NotBlank @Size(max=50) private String lastname;
 @Size(max=255) private String intro;
 @Size(max=2000) private String profile;
 public String getFirstname(){return firstname;} public void setFirstname(String s){firstname=s;}
 public String getLastname(){return lastname;} public void setLastname(String s){lastname=s;}
 public String getIntro(){return intro;} public void setIntro(String s){intro=s;}
 public String getProfile(){return profile;} public void setProfile(String s){profile=s;}
}
