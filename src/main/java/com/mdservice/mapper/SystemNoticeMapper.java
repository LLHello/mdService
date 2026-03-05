package com.mdservice.mapper;

import com.mdservice.entity.SystemNotice;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SystemNoticeMapper {
    // 1. 发布公告
    @Insert("INSERT INTO system_notice(content, create_time) VALUES(#{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SystemNotice notice);

    // 2. 查询用户未读公告 (ID > lastReadId)
    @Select("SELECT * FROM system_notice WHERE id > #{lastReadId} ORDER BY id ASC")
    List<SystemNotice> selectUnreadNotices(Long lastReadId);

    // 3. 获取用户最后读取的公告ID
    @Select("SELECT last_read_notice_id FROM user_notice_read WHERE user_id = #{userId}")
    Long getLastReadNoticeId(String userId);

    // 4. 更新用户读取进度 (如果没有记录就插入，有则更新 - UPSERT)
    @Insert("INSERT INTO user_notice_read (user_id, last_read_notice_id) VALUES (#{userId}, #{lastReadId}) " +
            "ON DUPLICATE KEY UPDATE last_read_notice_id = #{lastReadId}")
    void updateLastReadId(String userId, Long lastReadId);
    @Delete("delete from system_notice where id = #{id}")
    Boolean deleteSysteNotice(String id);
    @Update("update system_notice set content = #{content} where id = #{id}")
    Boolean modifySystemNotice(SystemNotice systemNotice);
    @Select("select * from system_notice order by create_time DESC")
    List<SystemNotice> getAllSystemNotice();
}
