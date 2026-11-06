package sw2.lab5.Controllers;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.ui.Model;
import sw2.lab5.Service.CurrentUser;
import sw2.lab5.Entity.Usuario;
@ControllerAdvice public class ViewUser {
 private final CurrentUser current;
 public ViewUser(CurrentUser current){this.current=current;}
 @ModelAttribute public void user(Authentication auth, Model model){
  if(auth!=null && !(auth instanceof AnonymousAuthenticationToken)){
   Usuario u=current.get(auth); model.addAttribute("viewer",u);model.addAttribute("isAdmin",current.admin(u));
  }
 }
}
