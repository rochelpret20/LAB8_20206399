package sw2.lab5.Repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import sw2.lab5.Entity.FriendRequest;
import java.util.*;
public interface FriendRequestRepository extends JpaRepository<FriendRequest,Long>{
 @Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
 @Query("select f from FriendRequest f where f.id=:id") Optional<FriendRequest> lockRequest(@Param("id") long id);
 boolean existsByLowIdAndHighId(int low,int high);
 @Query("select f from FriendRequest f join fetch f.sender join fetch f.recipient where f.lowId=:id or f.highId=:id order by f.id desc")
 List<FriendRequest> involving(@Param("id") int id);
}
