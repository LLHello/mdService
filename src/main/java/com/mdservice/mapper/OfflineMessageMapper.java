package com.mdservice.mapper;

import com.mdservice.entity.OfflineMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OfflineMessageMapper {
    @Insert("INSERT INTO offline_message(user_id,content, status, create_time) VALUES(#{userId}, #{content}, 0, NOW())")
    void insert(OfflineMessage message);

    @Select("SELECT * FROM offline_message WHERE user_id = #{userId} AND status = 0 ORDER BY create_time ASC")
    List<OfflineMessage> selectUnreadByUserId(String userId);

    @Update("UPDATE offline_message SET status = 1 WHERE id = #{id}")
    void markAsRead(Long id);
    @Select("select * from offline_message where user_id = #{userId}")
    List<OfflineMessage> getAllSystemToUser(String userId);
}
