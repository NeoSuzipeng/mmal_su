package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;

import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ContextedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Created by 10353 on 2018/1/4.
 *
 */
@Service("iUserService")
public class IUserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUserName(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //MD5加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //将返回给前台的信息中密码置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if(StringUtils.isNotBlank(type)){
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0)
                    return ServerResponse.createByErrorMessage("Email已存在");
            }else if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUserName(str);
                if(resultCount > 0)
                    return ServerResponse.createByErrorMessage("用户已存在");
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccess("检验成功");
    }

    @Override
    public ServerResponse<String> register(User user){
        //检验必要信息的有效性
        ServerResponse<String> usernameValidResponse = checkValid(user.getUsername(), Const.USERNAME);
        if(!usernameValidResponse.isSuccess()){
            return usernameValidResponse;
        }
        ServerResponse<String> emailValidResponse = checkValid(user.getEmail(), Const.EMAIL);
        if(!emailValidResponse.isSuccess()){
            return emailValidResponse;
        }

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        user.setRole(Const.Role.ROLE_CUSTOMER);

        int resultCount = userMapper.insert(user);
        if(resultCount == 0)
            return ServerResponse.createByErrorMessage("注册失败");
        return ServerResponse.createBySuccess("注册成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        //检验必要信息的有效性
        ServerResponse<String> usernameValidResponse = checkValid(username, Const.USERNAME);
        if(usernameValidResponse.isSuccess()){
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        //检查问题是否为空
        String question = userMapper.selectQuestionByUserName(username);

        //todo question泛型问题
        if(StringUtils.isNotBlank(question))
            return ServerResponse.createBySuccess(null,question);
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount != 0){
            String token = UUID.randomUUID().toString();
            // todo 将token加入本地缓存中
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, token);
            return ServerResponse.createBySuccess(null,token);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        //检查Token是否为空
        if(StringUtils.isBlank(forgetToken))
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        //检查用户名有效性
        ServerResponse<String> validUserName = checkValid(username, Const.USERNAME);
        if(validUserName.isSuccess())
            return ServerResponse.createByErrorMessage("用户不存在");
        //从本地缓存中获取Token
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(token == null){
            //Token过期
            return ServerResponse.createByErrorMessage("token已经失效");
        }
        if(StringUtils.equals(token,forgetToken)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.updatePasswordByUserName(username, md5Password);
            if(resultCount > 0)
                return ServerResponse.createBySuccess("修改成功");

        }else{
            //Token错误
            return ServerResponse.createByErrorMessage("token错误");
        }
        return ServerResponse.createByErrorMessage("修改密码操作失效");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordNew, String passwordOld, User user) {
        //防止横向越权，如果登录用户在参数中传入随便传入一个在数据库中的密码，在检验密码是否存在时就会通过
        int resultCount = userMapper.validPasswordOldByUserId(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码输入错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if(resultCount > 0)
            return ServerResponse.createBySuccess("修改密码成功");
        return ServerResponse.createByErrorMessage("修改密码错误");
    }

    @Override
    public ServerResponse<User> updateUserInformation(User user) {
        //检验Email是否已经在数据库中存在，并且不属于本用户
        int resultCount = userMapper.validEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0)
            return ServerResponse.createByErrorMessage("Email已经存在");

        User updateUser = new User();
        //用户名和Id不进行更新
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setUpdateTime(new Date());
        resultCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(resultCount > 0)
            return ServerResponse.createBySuccess(updateUser);
        return ServerResponse.createByError();
    }

    @Override
    public ServerResponse<User> getInformation(Integer id) {
        User user = userMapper.selectByPrimaryKey(id);
        if(user == null)
            return ServerResponse.createByErrorMessage("无此用户");
        //将用户密码置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


    //后台功能
    //检验管理员的权限
    @Override
    public ServerResponse checkAdmin(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN)
            return ServerResponse.createBySuccess();
        return ServerResponse.createByError();
    }
}
