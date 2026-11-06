package sw2.lab5.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller public class LoginController {
 @GetMapping("/loginForm") public String login(){return "index";}
 @GetMapping("/redirectByRol") public String redirect(){return "redirect:/post";}
}
