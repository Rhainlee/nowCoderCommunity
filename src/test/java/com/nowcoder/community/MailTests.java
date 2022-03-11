package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;


    /**
     * 由于每次使用maven clean install 都会执行测试，自动发送邮件，所以我暂时注释了邮件发送测试
     */

//    @Test
//    public void testTextMail(){
//        mailClient.sendMail("362160001@qq.com", "TEST", "welcome!");
//    }

//    @Test
//    public void testHtmlMail(){
//        //访问模板，需要给模板传入动态参数(比如这里是username)，用Context来构造这个参数
//        Context context = new Context();
//        context.setVariable("username", "花生皮不皮");
//
//        //返回html网页，指定模板路径，传入参数
//        String content = templateEngine.process("/mail/demo", context);
//        System.out.println(content);
//
//        mailClient.sendMail("362160001@qq.com", "HTML", content);
//    }
}
