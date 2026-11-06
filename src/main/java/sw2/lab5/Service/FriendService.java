package sw2.lab5.Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import sw2.lab5.Entity.*;
import sw2.lab5.Repository.*;
import java.util.*;
@Service @Transactional public class FriendService {
 private final UsuarioRepository users;private final FriendRequestRepository requests;
 public FriendService(UsuarioRepository users,FriendRequestRepository requests){this.users=users;this.requests=requests;}
 public List<FriendRequest> list(int id){return requests.involving(id);}
 public void send(Usuario sender,int recipientId){
  int id=sender.getId_user();
  if(id==recipientId)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"No puedes enviarte una solicitud");
  int low=Math.min(id,recipientId),high=Math.max(id,recipientId);
  // Lock both participants in a stable order; serialize reciprocal requests.
  Usuario first=users.lockUser(low).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  Usuario second=users.lockUser(high).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  Usuario recipient=recipientId==low?first:second;
  if(!first.isActive() || !second.isActive())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Usuario inactivo");
  if(requests.existsByLowIdAndHighId(low,high))throw new ResponseStatusException(HttpStatus.CONFLICT,"Ya existe una solicitud o amistad");
  FriendRequest f=new FriendRequest();f.setLowId(low);f.setHighId(high);f.setSender(sender);f.setRecipient(recipient);requests.save(f);
 }
 public void respond(long id,Usuario recipient,boolean accept){
  FriendRequest f=requests.lockRequest(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(f.getRecipient().getId_user()!=recipient.getId_user())throw new AccessDeniedException("Solo el destinatario responde");
  if(!"PENDING".equals(f.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Solicitud ya resuelta");
  if(accept){f.setStatus("ACCEPTED");requests.save(f);}else requests.delete(f);
 }
}
