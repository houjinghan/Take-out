package com.blog.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.Result;
import com.blog.entity.User;
import com.blog.service.UserService;
import com.blog.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {
        String phone = user.getPhone();
        if (!phone.isEmpty()) {
            //随机生成一个验证码
            String code = MailUtils.achieveCode();
            log.info(code);
            //这里的phone其实就是邮箱，code是我们生成的验证码
//            MailUtils.sendTestMail(phone, code);
            //验证码存session，方便后面拿出来比对
//            session.setAttribute(phone, code);
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return Result.success("验证码发送成功");
        }
        return Result.error("验证码发送失败");
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map, HttpSession session) {
        log.info(map.toString());
        String phone = map.get("phone").toString();
        log.info(phone);
        String code = map.get("code").toString();
        //把刚刚存进去的验证码拿出来
        Object codeInRedis = redisTemplate.opsForValue().get(phone);
        log.info("你输入的code{}，redis中的code{}，计算结果为{}", code, codeInRedis, (code != null && code.equals(codeInRedis)));
        //看看接收到用户输入的验证码是否和session中的验证码相同
        if (code != null && code.equals(codeInRedis)) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {  //插入数据库
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            redisTemplate.delete(phone);
            return Result.success(user);
        }
        return Result.error("登录失败");
    }

    @PostMapping("/loginout")
    public Result<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return Result.success("退出成功");
    }
}
