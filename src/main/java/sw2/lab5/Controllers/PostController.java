package sw2.lab5.Controllers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.validation.Valid;
import sw2.lab5.Dto.PostForm;
import sw2.lab5.Entity.Post;
import sw2.lab5.Service.*;
@Controller @RequestMapping("/post") public class PostController {
 private final PostService posts;private final CurrentUser current;
 public PostController(PostService posts,CurrentUser current){this.posts=posts;this.current=current;}
 @GetMapping({"","/list"}) public String list(Model m,Authentication a){m.addAttribute("lista",posts.list(current.get(a)));return "post/lista";}
 @GetMapping("/create") public String create(Model m){m.addAttribute("postForm",new PostForm());return "post/form";}
 @GetMapping("/edit") public String edit(@RequestParam int id,Model m,Authentication a){
  Post p=posts.editable(id,current.get(a));PostForm f=new PostForm();f.setId(id);f.setTitle(p.getTitle());f.setSummary(p.getSummary());f.setContent(p.getContent());f.setPublished(p.isPublished());
  m.addAttribute("postForm",f);return "post/form";
 }
 @PostMapping("/guardar") public String save(@Valid @ModelAttribute PostForm postForm,BindingResult errors,Authentication a,RedirectAttributes flash){
  if(postForm.getId()!=0)posts.editable(postForm.getId(),current.get(a));
  if(errors.hasErrors())return "post/form";
  posts.save(postForm,current.get(a));flash.addFlashAttribute("msg","Post guardado");return "redirect:/post";
 }
 @PostMapping("/borrar") public String delete(@RequestParam int id,Authentication a,RedirectAttributes flash){
  posts.delete(id,current.get(a));flash.addFlashAttribute("msg","Post eliminado");return "redirect:/post";
 }
}
