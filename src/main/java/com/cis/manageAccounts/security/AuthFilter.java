package com.cis.manageAccounts.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthFilter extends OncePerRequestFilter {

    @Value("${auth.header}")
    private String TOKEN_HEADER;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private AdminService adminService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       String header= request.getHeader(TOKEN_HEADER);
       final SecurityContext securityContext=
               SecurityContextHolder.getContext();

       if(header!=null&&securityContext.getAuthentication()==null){
           String token=header.substring("Bearer ".length());
           String username=tokenUtil.getUsernameFromToken(token);
           if(username!=null)
           {
               UserDetails userDetails=adminService.loadUserByUsername(username);
               if(tokenUtil.isTokenValid(token,userDetails)){
                   UsernamePasswordAuthenticationToken authenticationToken=
                           new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                   authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                   SecurityContextHolder.getContext().setAuthentication(authenticationToken);
               }
           }
       }
       filterChain.doFilter(request,response);
    }
}
