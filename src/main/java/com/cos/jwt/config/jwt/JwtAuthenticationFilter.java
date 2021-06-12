package com.cos.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;

// 스프링시큐리티 에서 UsernamePasswordAuthenticationFilter 가 있음
// login 요청 해서 username, password 전송하면 (post)
// UsernamePasswordAuthenticationFilter 가 동작함
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    //  login  요청을 하면 로그인 시도를 위해 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter: 로그인 시도중");

        //  1.  username , password 받아서

            try{
//                BufferedReader br = request.getReader();
//
//                String input =null;
//                while ((input=br.readLine())!=null){
//                    System.out.println(input);
//                }
                ObjectMapper om = new ObjectMapper();
                User user = om.readValue(request.getInputStream(),User.class);
                System.out.println(user);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(user.getUsername(),user.getPassword());

                // PrincipalDetailsService  의  loadUserByUsername() 함수가 실행됨  그 후 정상이면 authentication 가 리턴
                Authentication authentication =
                        authenticationManager.authenticate(authenticationToken);

                //  authentication 객체가 session 영역에 저장됨 -> 로그인이 되었다는 뜻
                PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal();
                System.out.println("로그인 완료: " + principalDetails.getUser().getUsername());       // 값이 있으면 로그인이 정상적으로 된 것.
                //  authentication 객체가 session 영역에 저장을 해야하고 , 그 방법이 return 해주어야 함
                //  리턴의 이유는 권한관리를 (시큐리티)가 대신 해주기 때문 .. 편리하기 때문
                //  굳이 JWT  토큰을 사용하면서 세션을 만들 이유가 없음     .. 단지 권한처리 때문에 Session에 넣는 것
                return authentication;

            } catch (IOException e) {
                e.printStackTrace();
            } return null;

        //  2.  정상인지 로그인 시도 . AuthenticationManager 로 로그인 시도를 하면
        //                          PrincipalDetails 가 호출 되고 loadUserByUsername() 함수가 실행됨

        //  3.  PrincipalDetails 을 세션에 담고     (권한처리를 위해 세션에 담는 것)

        //  4.  JWT 토큰을 만들어서 응답 해주면 됨

    }

    //  attemptAuthentication 실행후 인증이 정상적으로 되었으면 successfulAuthentication 함수가 실행 된다.
    //  JWT 토큰을 만들어서 request 요청한 사용자에게 JWT 토큰을 response 해주면 됨.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication 실행됨: 인증이 완료됨");
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        // RSA 방식은 아니고  HASH 암호 방식임
        String jwtToken = JWT.create()
                .withSubject("cos토큰")                                                   //  토큰 이름
                .withExpiresAt(new Date(System.currentTimeMillis()+(60000*10)))          //  토큰 유효 시간   (10분 = 1분*10)
                .withClaim("id",principalDetails.getUser().getId())                 // 내가 넣고 싶은 키 값 넣으면 됨
                .withClaim("username",principalDetails.getUser().getUsername())
                .sign(Algorithm.HMAC512("cos"));

        response.addHeader("Authorization","Bearer " + jwtToken);               //  Bearer 띄고 써야함
    }
}
