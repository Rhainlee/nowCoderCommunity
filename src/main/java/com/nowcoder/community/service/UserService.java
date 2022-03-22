package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private HostHolder hostHolder;

    //注入域名，即注入一个固定值用@Value注解
    @Value("${community.path.domain}")
    private String domain;

    //注入项目名，即注入一个固定值用@Value注解
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;


    // 使用Redis缓存重构代码
    public User findUserById(int id){

        //return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    //注册业务
    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null){
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null){
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        //注册用户
        //密码加密
        //生成随机字符串
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setStatus(0);//没有激活
        user.setActivationCode(CommunityUtil.generateUUID());//生成激活码
        //设置随机头像
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user); //因为mybatis.configuration.useGeneratedKeys=true，所以insert时自动生成id

        //发送激活邮件activation.html
        //访问模板，需要给模板传入动态参数(比如这里是email+url)，用Context来构造这个参数,感觉和model作用很像啊
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //指定一个路径来处理激活请求
        //http://localhost:8080/community/activation/101/code   (101是用户id,code是激活码)
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

        //返回html网页，指定模板路径，传入参数
        String content = templateEngine.process("/mail/activation", context);
        //System.out.println(content);

        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    //激活业务
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId); //此处是不是应该先做空值判断，万一链接伪造了用户id呢！很有可能呀！
        if (user == null){   //空值判断，查不到用户则激活失败
            return ACTIVATION_FAILURE;
        }

        if (user.getStatus() == 1){ //已经激活过，这是重复激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId, 1);
            clearCache(userId); //清除缓存
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    //登录业务
    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg", "该账号不存在");
            return map;
        }
        //验证激活
        if(user.getStatus() == 0){
            map.put("usernameMsg", "该账号未激活");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg", "密码不正确");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        //loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket){

        //loginTicketMapper.updateStatus(ticket, 1); //状态改为无效
        //思路：先把凭证取出来，改完状态后再存回去
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    // 获取登录凭证
    public LoginTicket findLoginTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);

        //return loginTicketMapper.selectByTicket(ticket);
    }

    // 更新头像链接
    public int updateHeader(int userId, String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    // 修改密码
    public Map<String, Object> updatePassword(String oldPassword, String newPassword, String confirmPassword){

        Map<String, Object> map = new HashMap<>();
        // 空值及错误输入处理
        if (StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg", "原密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg", "新密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(confirmPassword)){
            map.put("confirmPasswordMsg", "确认密码不能为空");
            return map;
        }
        if (!newPassword.equals(confirmPassword)){
            map.put("confirmPasswordMsg", "两次密码输入不一致");
            return map;
        }

        User user = hostHolder.getUser();//获取user对象
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!oldPassword.equals(user.getPassword())){
            map.put("oldPasswordMsg", "原密码输入错误");
            return map;
        }

        //可以修改密码了！
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newPassword);
        clearCache(user.getId());
        map.put("msg", "您已成功修改密码,请重新登录");


        return map;
    }

    public User findUserByName(String username) {
        return  userMapper.selectByName(username);
    }

    // 1.当查询时优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }
    // 2.取不到时则从Mysql中取值并初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);  //一小时后清除缓存
        return user;
    }
    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    // 获取用户的权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
