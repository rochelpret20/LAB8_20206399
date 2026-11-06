package sw2.lab5.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sw2.lab5.Service.*;
import sw2.lab5.Repository.UsuarioRepository;
import sw2.lab5.Entity.Usuario;
import java.util.stream.Collectors;
@Controller @RequestMapping("/friends") public class FriendController {
 private final FriendService friends;private final CurrentUser current;private final UsuarioRepository users;
 public FriendController(FriendService friends,CurrentUser current,UsuarioRepository users){this.friends=friends;this.current=current;this.users=users;}
 @GetMapping public String list(Model m,Authentication a){
  Usuario u=current.get(a);java.util.List<sw2.lab5.Entity.FriendRequest> rows=friends.list(u.getId_user());
  m.addAttribute("requests",rows);m.addAttribute("connected",rows.stream().map(r->r.getSender().getId_user()==u.getId_user()?r.getRecipient().getId_user():r.getSender().getId_user()).collect(Collectors.toSet()));
  m.addAttribute("people",users.findAll().stream().filter(x->x.isActive()&&x.getId_user()!=u.getId_user()).collect(Collectors.toList()));return "friends/list";
 }
 @PostMapping("/send") public String send(@RequestParam int recipientId,Authentication a,RedirectAttributes flash){friends.send(current.get(a),recipientId);flash.addFlashAttribute("msg","Solicitud enviada");return "redirect:/friends";}
 @PostMapping("/{id}/accept") public String accept(@PathVariable long id,Authentication a){friends.respond(id,current.get(a),true);return "redirect:/friends";}
 @PostMapping("/{id}/reject") public String reject(@PathVariable long id,Authentication a){friends.respond(id,current.get(a),false);return "redirect:/friends";}
}
