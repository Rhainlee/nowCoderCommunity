package com.nowcoder.community.service;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    // 查询用户的会话列表
    public List<Message> findConversations(int userId, int offset, int limit) {
        return messageMapper.selectConversations(userId, offset, limit);
    }

    // 查询用户的会话数量
    public int findConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    // 查询用户某一会话的消息列表
    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return messageMapper.selectLetters(conversationId, offset, limit);
    }

    // 查询用户某一会话的消息数量
    public int findLetterCount(String conversationId) {
        return messageMapper.selectLetterCount(conversationId);
    };

    // 查询用户未读消息数量
    public int findLetterUnreadCount(int userId, String conversationId) {
        return messageMapper.selectLetterUnreadCount(userId, conversationId);
    }



}
