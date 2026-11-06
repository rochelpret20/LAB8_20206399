package sw2.lab5.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sw2.lab5.Dto.ProfileForm;
import sw2.lab5.Entity.Usuario;
import sw2.lab5.Repository.UsuarioRepository;
import sw2.lab5.Service.CurrentUser;
import javax.validation.Valid;
@Controller @RequestMapping("/user") public class UsuarioController {
 private final UsuarioRepository users;private final CurrentUser current;
 public UsuarioController(UsuarioRepository users,CurrentUser current){this.users=users;this.current=current;}
 @GetMapping("/list") public String list(Model m){m.addAttribute("listausuarios",users.findAll());return "user/list";}
 @GetMapping({"","/edit"}) public String profile(Model m,Authentication a){
  Usuario u=current.get(a);ProfileForm f=new ProfileForm();f.setFirstname(u.getFirstname());f.setLastname(u.getLastname());f.setIntro(u.getIntro());f.setProfile(u.getProfile());m.addAttribute("profileForm",f);return "user/form";
 }
 @PostMapping("/guardar") public String save(@Valid @ModelAttribute ProfileForm profileForm,BindingResult errors,Authentication a,RedirectAttributes flash){
  if(errors.hasErrors())return "user/form";
  Usuario u=current.get(a);u.setFirstname(profileForm.getFirstname());u.setLastname(profileForm.getLastname());u.setIntro(profileForm.getIntro());u.setProfile(profileForm.getProfile());users.save(u);
  flash.addFlashAttribute("msg","Perfil actualizado");return "redirect:/user";
 }
}
