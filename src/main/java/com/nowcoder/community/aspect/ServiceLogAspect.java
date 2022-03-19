package com.nowcoder.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {


    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    // 定义切点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))") // 所有的业务组件，所有的方法，所有的参数。所有的返回值都要处理
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        // 用户[1.2.3.4]，在[xxx]，访问了[com.nowcoder.community.service.xxx()]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return; // 不记日志了，也可以做其他处理
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost(); //获取到ip地址
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());  // 获取当前格式化时间
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName(); //获取目标的类.方法
        logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
    }
}
