package sw2.lab5.Repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import javax.persistence.LockModeType;
import sw2.lab5.Entity.Usuario;
import java.util.Optional;
public interface UsuarioRepository extends JpaRepository<Usuario,Integer>{
 Usuario findByEmail(String email);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select u from Usuario u where u.id_user=:id")
 Optional<Usuario> lockUser(@Param("id") int id);
}
