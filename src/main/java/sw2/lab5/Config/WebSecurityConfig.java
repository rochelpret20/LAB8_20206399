package sw2.lab5.Config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import javax.sql.DataSource;
@Configuration
public class WebSecurityConfig {
 @Bean public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
  http.authorizeRequests()
   .antMatchers("/", "/loginForm", "/css/**", "/js/**", "/webfonts/**", "/error").permitAll()
   .antMatchers("/user/list", "/post/borrar").hasAuthority("admin")
   .antMatchers("/post/**", "/user/**", "/friends/**", "/redirectByRol").hasAnyAuthority("user", "admin")
   .anyRequest().denyAll();
  http.formLogin().loginPage("/loginForm").loginProcessingUrl("/processlogin")
   .defaultSuccessUrl("/post", true).failureUrl("/loginForm?error").permitAll();
  http.logout().logoutSuccessUrl("/loginForm?logout").deleteCookies("JSESSIONID", "SESSION").invalidateHttpSession(true);
  http.sessionManagement().sessionFixation().migrateSession();
  return http.build();
 }
 @Bean public UserDetailsService userDetailsService(DataSource source) {
  JdbcUserDetailsManager m=new JdbcUserDetailsManager(source);
  m.setUsersByUsernameQuery("select email,password,active from user where email=?");
  m.setAuthoritiesByUsernameQuery("select u.email,r.role_name from user u join role r on u.id_role=r.id_role where u.email=? and u.active=1");
  return m;
 }
 @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
