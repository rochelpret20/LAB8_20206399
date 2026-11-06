package sw2.lab5.Service;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import sw2.lab5.Entity.Usuario;
import sw2.lab5.Repository.UsuarioRepository;
@Service public class CurrentUser {
 private final UsuarioRepository users;
 public CurrentUser(UsuarioRepository users){this.users=users;}
 public Usuario get(Authentication auth){
  Usuario u=auth==null?null:users.findByEmail(auth.getName());
  if(u==null || !u.isActive()) throw new AccessDeniedException("Sesión no válida");
  return u;
 }
 public boolean admin(Usuario u){return "admin".equals(u.getId_role().getRole_name());}
}
