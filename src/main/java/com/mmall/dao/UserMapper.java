package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUserName(String username);

    User selectLogin(@Param("username") String username, @Param("password")  String password);

    int checkEmail(String email);

    String selectQuestionByUserName(String username);

    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    int updatePasswordByUserName(@Param("username") String username, @Param("passwordNew") String passwordNew);

    int validPasswordOldByUserId(@Param("passwordOld") String passwordOld,@Param("id") Integer id);

    int validEmailByUserId(@Param("email") String email,@Param("id") Integer id);
}