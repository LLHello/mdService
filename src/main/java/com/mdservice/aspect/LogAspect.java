package com.mdservice.aspect;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mdservice.aop.Log;
import com.mdservice.entity.SysLog;
import com.mdservice.entity.User;
import com.mdservice.mapper.SysLogMapper;
import com.mdservice.service.inter.SysLogService;
import com.mdservice.utils.UserLocal;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.net.IPv6Utils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

@Aspect
@Component
public class LogAspect {
    @Resource
    private SysLogService sysLogService;

    /** 切入点：拦截所有加了@Log注解的方法 */
    @Pointcut("@annotation(com.mdservice.aop.Log)")
    public void sysLogPointcut() {}

    /** 环绕通知：执行方法前后记录日志 */
    @Around("sysLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysLog log = new SysLog();
        Object resultObj = null;
        try {
            resultObj = joinPoint.proceed();
            if (resultObj instanceof Flux || resultObj instanceof Mono) {
                resultObj = "[响应式数据流(Flux/Mono)，不进行JSON序列化打印]";
            }
            log.setResult("成功");
            return resultObj;
        } catch (Exception e) {
            log.setResult("失败");
            log.setException(StrUtil.sub(e.toString(), 0, 500));
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            this.fillLogInfo(joinPoint, log, endTime - startTime);
            // 调用 Service 保存日志
            sysLogService.saveSysLog(log);
        }
    }

    /** 填充日志详情 */
    private void fillLogInfo(ProceedingJoinPoint joinPoint, SysLog log, long timeCost) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log sysLogAnnotation = method.getAnnotation(Log.class);

        // 填充注解信息
        log.setModule(sysLogAnnotation.module());
        log.setOperation(sysLogAnnotation.operation());
        log.setDescription(sysLogAnnotation.desc());

        // 填充请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.setRequestUrl(request.getRequestURI());
            log.setRequestMethod(request.getMethod());
        }

        // 填充方法和参数信息
        log.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
        log.setParams(JSONUtil.toJsonStr(joinPoint.getArgs()));
        log.setTimeCost(timeCost);
        // 操作的用户id
        String userId = UserLocal.getUser();
        log.setUsername(userId);
    }
}

