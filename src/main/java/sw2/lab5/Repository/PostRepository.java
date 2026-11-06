package sw2.lab5.Repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import sw2.lab5.Entity.Post;
import java.util.List;
public interface PostRepository extends JpaRepository<Post,Integer>{
 @Query("select p from Post p join fetch p.user order by p.id_Post desc") List<Post> findAllByOrderByIdDesc();
 @Query("select p from Post p join fetch p.user where p.published=true or p.user.id_user=:id order by p.id_Post desc")
 List<Post> visible(@Param("id") int id);
}
