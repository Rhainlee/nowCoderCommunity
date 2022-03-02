package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    private  static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;


    //访问注册页面
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    //访问登录页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }  //返回login.html,里面包含图片路径，浏览器通过路径再次访问服务器获得验证码图片


    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //注意这里返回类型为void,因为这里向浏览器输出一个图片，不是字符串，也不是html
        //我们需要用Response对象手动向浏览器输出
        //我们生成完验证码之后，服务端需要记住，再次访问时用来检验验证码是否正确---跨请求用session/cookie
        //验证码不能存在浏览器端（cookie），否则很容易被盗取

        // 生成验证码 （需要先获取Bean并注入到容器之中）
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码存入session
        session.setAttribute("kaptcha", text);

        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();//获取输出流，图片用字节流比较好
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败" + e.getMessage());
        }

    }
//    //处理登录请求
//    @RequestMapping(path = "/login", method = RequestMethod.POST)
//    public String login(Model model, User user){
//        if (user == null){
//            return "/site/login";
//        }
//
//        //findUserByUsernameAndPassword;
//        return "/index";
//    }


    //处理注册请求
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {  //只要页面传入的值与user的属性相匹配，Spring MVC就会把值注入属性
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET) //http://localhost:8080/community/activation/101/code   (101是用户id,code是激活码)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target", "/login");//激活成功后跳转到登录页面，还没写呢？？
        } else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，您的账号已经激活过了！");
            model.addAttribute("target", "/index");//激活过，跳到首页
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活码不正确！");
            model.addAttribute("target", "/index");//跳到首页
        }
        model.addAttribute("emailMsg", userService.findUserById(userId).getEmail());
        return "/site/operate-result";
    }
}
