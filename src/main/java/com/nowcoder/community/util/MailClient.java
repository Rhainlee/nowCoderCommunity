package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    //声明logger记录日志
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    //将JavaMailSender组件注入到当前Bean中
    @Autowired
    private JavaMailSender mailSender;
//    发送方固定，所以直接注入到Bean中 @Value
    @Value("${spring.mail.username}")
    private String from;
    //发送方接收方发送标题发送内容
    public void sendMail(String to, String subject, String content){

        try {
            //    create+send
            MimeMessage message = mailSender.createMimeMessage();
            //    使用MimeMessageHelper来构建信息
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败：" + e.getMessage());
        }
    }



}
