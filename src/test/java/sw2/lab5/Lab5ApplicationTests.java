package sw2.lab5;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.servlet.http.Cookie;
import sw2.lab5.Repository.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class Lab5ApplicationTests {
 @Autowired MockMvc mvc;
 @Autowired JdbcTemplate db;
 @Autowired PasswordEncoder encoder;
 @Autowired PostRepository posts;
 @Autowired FriendRequestRepository friends;
 @BeforeEach void seed(){
  db.update("delete from SPRING_SESSION_ATTRIBUTES");db.update("delete from SPRING_SESSION");
  db.update("delete from friend_request");db.update("delete from post");db.update("delete from user");db.update("delete from role");
  db.update("insert into role(id_role,role_name) values(1,'admin'),(2,'user')");
  for(int i=1;i<=4;i++)db.update("insert into user(id_user,firstName,lastName,email,password,active,id_role) values(?,?,?,?,?,?,?)",i,"Persona"+i,"Prueba","u"+i+"@test.pe",encoder.encode("Clave123!"),i!=4,i==3?1:2);
  db.update("insert into post(id_post,author_id,title,summary,content,published) values(10,1,'Propio','Resumen','Contenido',true),(11,2,'Ajeno','Resumen','Contenido',true),(12,2,'Secreto','Resumen','Borrador privado',false)");
 }
 @Test void anonymousRequiresLogin()throws Exception{mvc.perform(get("/post")).andExpect(status().is3xxRedirection());mvc.perform(get("/loginForm")).andExpect(status().isOk());}
 @Test void listHidesOtherDrafts()throws Exception{
  mvc.perform(get("/post").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Borrador privado"))));
  mvc.perform(get("/post").with(user("u3@test.pe").authorities(()->"admin"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Borrador privado")));
 }
 @Test void editChecksOwnershipOnBothRoutes()throws Exception{
  mvc.perform(get("/post/edit?id=10").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isOk());
  mvc.perform(get("/post/edit?id=11").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isForbidden());
  mvc.perform(post("/post/guardar").with(user("u1@test.pe").authorities(()->"user")).with(csrf()).param("id","11").param("title","Ataque").param("content","Texto")).andExpect(status().isForbidden());
  assertEquals("Ajeno",posts.findById(11).get().getTitle());
 }
 @Test void saveOwnPostAndIgnoreForgedAuthor()throws Exception{
  mvc.perform(post("/post/guardar").with(user("u1@test.pe").authorities(()->"user")).with(csrf()).param("id","10").param("title","Actualizado").param("summary","ñ".repeat(255)).param("content","Contenido actualizado").param("user.id_user","2")).andExpect(redirectedUrl("/post"));
  assertEquals("Actualizado",posts.findById(10).get().getTitle());assertEquals(1,posts.findById(10).get().getUser().getId_user());
 }
 @Test void createUsesAuthenticatedAuthor()throws Exception{
  mvc.perform(post("/post/guardar").with(user("u1@test.pe").authorities(()->"user")).with(csrf()).param("title","Nuevo").param("content","Contenido").param("user.id_user","3")).andExpect(redirectedUrl("/post"));
  assertTrue(posts.findAll().stream().anyMatch(p->p.getTitle().equals("Nuevo")&&p.getUser().getId_user()==1));
 }
 @Test void adminEditsOthersWithoutChangingAuthor()throws Exception{
  mvc.perform(post("/post/guardar").with(user("u3@test.pe").authorities(()->"admin")).with(csrf()).param("id","11").param("title","Moderado").param("content","Texto")).andExpect(redirectedUrl("/post"));
  assertEquals(2,posts.findById(11).get().getUser().getId_user());
 }
 @Test void onlyAdminDeletesAndGetNeverDeletes()throws Exception{
  mvc.perform(post("/post/borrar?id=10").with(user("u1@test.pe").authorities(()->"user")).with(csrf())).andExpect(status().isForbidden());
  mvc.perform(get("/post/borrar?id=10").with(user("u3@test.pe").authorities(()->"admin"))).andExpect(status().isMethodNotAllowed());assertTrue(posts.existsById(10));
  mvc.perform(post("/post/borrar?id=10").with(user("u3@test.pe").authorities(()->"admin")).with(csrf())).andExpect(redirectedUrl("/post"));assertFalse(posts.existsById(10));
 }
 @Test void csrfIsRequired()throws Exception{
  mvc.perform(post("/post/guardar").with(user("u1@test.pe").authorities(()->"user")).param("title","Mal").param("content","Texto")).andExpect(status().isForbidden());
  mvc.perform(post("/friends/send?recipientId=2").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isForbidden());
  mvc.perform(post("/logout").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isForbidden());
 }
 @Test void invalidInputAndMissingPosts()throws Exception{
  mvc.perform(post("/post/guardar").with(user("u1@test.pe").authorities(()->"user")).with(csrf()).param("title","").param("content","Texto")).andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("postForm","title"));
  mvc.perform(get("/post/edit?id=999").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isNotFound());
 }
 @Test void profileCannotEscalateRoleOrChangeOtherUser()throws Exception{
  mvc.perform(post("/user/guardar").with(user("u1@test.pe").authorities(()->"user")).with(csrf()).param("firstname","Nuevo nombre").param("lastname","Apellido").param("id_user","2").param("id_role.id_role","1").param("active","false")).andExpect(redirectedUrl("/user"));
  assertEquals("Nuevo nombre",db.queryForObject("select firstName from user where id_user=1",String.class));assertEquals(2,db.queryForObject("select id_role from user where id_user=1",Integer.class));
  assertEquals("Persona2",db.queryForObject("select firstName from user where id_user=2",String.class));
 }
 @Test void usersListIsAdminOnly()throws Exception{
  mvc.perform(get("/user/list").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isForbidden());
  mvc.perform(get("/user/list").with(user("u3@test.pe").authorities(()->"admin"))).andExpect(status().isOk());
 }
 private void send()throws Exception{mvc.perform(post("/friends/send?recipientId=2").with(user("u1@test.pe").authorities(()->"user")).with(csrf())).andExpect(redirectedUrl("/friends"));}
 @Test void requestValidationAndReciprocalDuplicate()throws Exception{
  mvc.perform(post("/friends/send?recipientId=1").with(user("u1@test.pe").authorities(()->"user")).with(csrf())).andExpect(status().isBadRequest());
  mvc.perform(post("/friends/send?recipientId=4").with(user("u1@test.pe").authorities(()->"user")).with(csrf())).andExpect(status().isBadRequest());
  mvc.perform(post("/friends/send?recipientId=999").with(user("u1@test.pe").authorities(()->"user")).with(csrf())).andExpect(status().isNotFound());
  send();sendDuplicate("u1@test.pe",2);sendDuplicate("u2@test.pe",1);assertEquals(1,friends.count());
 }
 private void sendDuplicate(String email,int target)throws Exception{mvc.perform(post("/friends/send?recipientId="+target).with(user(email).authorities(()->"user")).with(csrf())).andExpect(status().isConflict());}
 @Test void recipientAloneAcceptsAndFriendshipIsVisible()throws Exception{
  send();long id=friends.findAll().get(0).getId();
  mvc.perform(post("/friends/"+id+"/accept").with(user("u3@test.pe").authorities(()->"admin")).with(csrf())).andExpect(status().isForbidden());
  mvc.perform(post("/friends/"+id+"/accept").with(user("u1@test.pe").authorities(()->"user")).with(csrf())).andExpect(status().isForbidden());
  mvc.perform(post("/friends/"+id+"/accept").with(user("u2@test.pe").authorities(()->"user")).with(csrf())).andExpect(redirectedUrl("/friends"));assertEquals("ACCEPTED",friends.findById(id).get().getStatus());
  mvc.perform(get("/friends").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("Amigos")));
  mvc.perform(post("/friends/"+id+"/accept").with(user("u2@test.pe").authorities(()->"user")).with(csrf())).andExpect(status().isConflict());
 }
 @Test void recipientCanRejectAndResend()throws Exception{
  send();long id=friends.findAll().get(0).getId();
  mvc.perform(post("/friends/"+id+"/reject").with(user("u2@test.pe").authorities(()->"user")).with(csrf())).andExpect(redirectedUrl("/friends"));assertEquals(0,friends.count());send();
 }
 @Test void realLoginLogoutAndInactiveAccount()throws Exception{
  mvc.perform(post("/processlogin").with(csrf()).param("username","u4@test.pe").param("password","Clave123!")).andExpect(unauthenticated());
  mvc.perform(post("/processlogin").with(csrf()).param("username","u1@test.pe").param("password","mal")).andExpect(unauthenticated());
  MvcResult result=mvc.perform(post("/processlogin").with(csrf()).param("username","u1@test.pe").param("password","Clave123!")).andExpect(authenticated().withUsername("u1@test.pe")).andExpect(redirectedUrl("/post")).andReturn();
  Cookie session=result.getResponse().getCookie("SESSION");assertNotNull(session);
  assertEquals(1,db.queryForObject("select count(*) from SPRING_SESSION where PRINCIPAL_NAME='u1@test.pe'",Integer.class));
  mvc.perform(get("/post").cookie(session)).andExpect(status().isOk());
  mvc.perform(post("/logout").cookie(session).with(csrf())).andExpect(redirectedUrl("/loginForm?logout")).andExpect(unauthenticated());
  assertEquals(0,db.queryForObject("select count(*) from SPRING_SESSION where PRINCIPAL_NAME='u1@test.pe'",Integer.class));
  mvc.perform(get("/post").cookie(session)).andExpect(status().is3xxRedirection());
 }
 @Test void profileAndCreateFormsRender()throws Exception{
  mvc.perform(get("/user").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isOk());
  mvc.perform(get("/post/create").with(user("u1@test.pe").authorities(()->"user"))).andExpect(status().isOk());
 }
}
