package sw2.lab5.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sw2.lab5.Entity.*;
import sw2.lab5.Dto.PostForm;
import sw2.lab5.Repository.PostRepository;
import java.util.List;
@Service @Transactional public class PostService {
 private final PostRepository posts; private final CurrentUser current;
 public PostService(PostRepository posts,CurrentUser current){this.posts=posts;this.current=current;}
 public List<Post> list(Usuario u){return current.admin(u)?posts.findAllByOrderByIdDesc():posts.visible(u.getId_user());}
 public Post editable(int id,Usuario u){
  Post p=posts.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!current.admin(u) && p.getUser().getId_user()!=u.getId_user()) throw new AccessDeniedException("No puedes editar este post");
  return p;
 }
 public void save(PostForm form,Usuario u){
  Post p=form.getId()==0?new Post():editable(form.getId(),u);
  if(form.getId()==0)p.setUser(u);
  p.setTitle(form.getTitle().trim()); p.setSummary(form.getSummary());p.setContent(form.getContent());p.setPublished(form.isPublished());
  posts.save(p);
 }
 public void delete(int id,Usuario u){
  if(!current.admin(u))throw new AccessDeniedException("Solo el administrador elimina posts");
  posts.delete(editable(id,u));
 }
}
