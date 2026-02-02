package com.mdservice.interceptor;

import com.mdservice.entity.User;
import com.mdservice.exception.AuthException;
import com.mdservice.utils.JwtUtil;
import com.mdservice.utils.UserLocal;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //因为 OPTIONS 请求 不携带 Token （它只是来问问“能不能发请求”），如果你的拦截器没有专门放行它，拦截器就会像对待普通请求一样去检查 Header 里的 Token。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        log.info("登录拦截：{},{},{}",request, response, handler);
        log.info("登录拦截路径：{}", request.getRequestURI());

        // 1. 从请求头中获取 Token
        // 前端通常约定 Header: "Authorization: Bearer <token>"
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader);
        if (!StringUtils.hasLength(authHeader) || !authHeader.startsWith("Bearer ")) {
//            return false;
            throw new RuntimeException("未携带 Token 或格式错误");
        }

        String token = authHeader.substring(7);
        log.info("token: {}", token);
        // 3. 解析 Token
        try {
            Claims claims = jwtUtil.parseToken(token);
            log.info("claims: {}", claims);
            // 4. 将解析出的用户信息放入 Request 域，方便后续 Controller 使用
//            request.setAttribute("currentUser", claims);
//            request.setAttribute("userId", claims.get("userId"));
            UserLocal.setUser(claims.get("userId").toString());
            log.info("token验证成功，放行！");
            return true; // 放行
        } catch (Exception e) {
            log.error("token验证失败!");
            response.setStatus(401);
            response.getWriter().write("Token invalid: " + e.getMessage());
            return false; // 拦截
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserLocal.removeUser();
    }
}
