package br.com.franklin.todolist.task.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.franklin.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
            
            var servletPath = request.getServletPath();

            if(servletPath.startsWith("/tasks/")){
                // Pegar autenticação
                var authorization = request.getHeader("Authorization");
                
                var authEnconded = authorization.substring("Basic".length()).trim();
                
                String authDecoded = new String(Base64.getDecoder().decode(authEnconded));
                System.out.println(authDecoded);
                
                String[] credentials = authDecoded.split(":");
        
                String username = credentials[0];
                String password = credentials[1];

                // Validar usuário

                var user = this.userRepository.findByUsername(username);
                
                if(user == null) {
                    response.sendError(401);
                } else {
                    
                    // Validar senha
                    
                    var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                    if(passwordVerify.verified) {
                        request.setAttribute("idUser", user.getId());
                        filterChain.doFilter(request, response);
                    } else {
                        response.sendError(401);
                    }
                }
            } else {
                filterChain.doFilter(request, response);
            }
            
    }

    
}
