//package com.nowcoder.community.aspect;
//
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.springframework.stereotype.Component;
//
//@Component
//@Aspect
//public class AlphaAspect {
//
//    // 定义切点
//    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))") // 所有的业务组件，所有的方法，所有的参数。所有的返回值都要处理
//    public void pointcut() {
//
//    }
//
//    // 定义通知
//    // 在连接点（这里就是指业务方法）开始时候做什么？结束时候做什么？返回是数据后做什么？抛异常时做什么？在前后都织入逻辑？
//
//    @Before("pointcut()")
//    public void before() {
//        System.out.println("before");
//    }
//
//    @After("pointcut()")
//    public void after() {
//        System.out.println("after");
//    }
//
//    @AfterReturning("pointcut()")
//    public void afterReturning() {
//        System.out.println("afterReturning");
//    }
//
//    @AfterThrowing("pointcut()")
//    public void afterThrowing() {
//        System.out.println("afterThrowing");
//    }
//
//    @Around("pointcut()")
//    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
//        System.out.println("around before");  // 调用之前做些啥
//        Object obj = joinPoint.proceed();  // 调用目标组件的方法
//        System.out.println("around after");  // 调用之后做些啥
//        return obj;
//    }
//
//}
