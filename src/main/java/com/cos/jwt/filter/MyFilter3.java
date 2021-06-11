package com.cos.jwt.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class MyFilter3 implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // 토큰 을 만들어 줘야함 . id, pw  정상적으로 들어와서 로그인이 완료 되면 토큰을 만들어주고 그걸 응답
        // 요청 할 때마다 header 에 AUTHORIZATION 에 value 값으로 토큰을 가지고 오면 넘어온 토큰이 내가 만든 토큰이 맞는지 검증 (RSA, HS256)

        // POST 전송시 연결
        if(req.getMethod().equals("POST")){
            System.out.println("POST 요청됨");
            String headerAuth = req.getHeader("Authorization");
            System.out.println("Authorization: " + headerAuth);
            System.out.println("Filter3");

            //  토큰(코스) 보유해야만 진행
            if(headerAuth.equals("cos")){
                chain.doFilter(req,res);
            }else {
                PrintWriter outPrintWriter = res.getWriter();
                outPrintWriter.println("인증안됨");
            }
        }

    }
}
