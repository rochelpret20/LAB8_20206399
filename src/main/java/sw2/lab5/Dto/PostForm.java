package sw2.lab5.Dto;
import javax.validation.constraints.*;
public class PostForm {
 @Min(0) private int id;
 @NotBlank(message="Escribe un título") @Size(max=75) private String title;
 @Size(max=255) private String summary;
 @NotBlank(message="Escribe el contenido") @Size(max=10000) private String content;
 private boolean published;
 public int getId(){return id;} public void setId(int id){this.id=id;}
 public String getTitle(){return title;} public void setTitle(String s){title=s;}
 public String getSummary(){return summary;} public void setSummary(String s){summary=s;}
 public String getContent(){return content;} public void setContent(String s){content=s;}
 public boolean isPublished(){return published;} public void setPublished(boolean b){published=b;}
}
