package sw2.lab5.Entity;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="friend_request",uniqueConstraints=@UniqueConstraint(columnNames={"low_id","high_id"}))
public class FriendRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="low_id",nullable=false) private int lowId;
 @Column(name="high_id",nullable=false) private int highId;
 @ManyToOne(optional=false) @JoinColumn(name="sender_id") private Usuario sender;
 @ManyToOne(optional=false) @JoinColumn(name="recipient_id") private Usuario recipient;
 @Column(nullable=false,length=12) private String status="PENDING";
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 public Long getId(){return id;} public int getLowId(){return lowId;} public void setLowId(int n){lowId=n;}
 public int getHighId(){return highId;} public void setHighId(int n){highId=n;}
 public Usuario getSender(){return sender;} public void setSender(Usuario u){sender=u;}
 public Usuario getRecipient(){return recipient;} public void setRecipient(Usuario u){recipient=u;}
 public String getStatus(){return status;} public void setStatus(String s){status=s;}
 public LocalDateTime getCreatedAt(){return createdAt;}
}
