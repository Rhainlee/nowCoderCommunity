package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootNode = new TrieNode();  //（构造之后subNodes引用不为空，而是其中的Character和TrieNode为空？）

    //
    @PostConstruct
    public void init() {
        // 把txt文件中的字符读出来，（从类路径下读取配置文件，类路径：classes目录下）
        // ctrl+f9编译如果txt文件没有出现在classes目录下，那就maven clean+compile


        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 字节流转换为字符流转换为缓冲流？？流找时间要学习了！
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        ){
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                this.addKeyword(keyword);

            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树中
    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode; //起指针作用
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,  subNode);
            }
            // 指向下个节点
            tempNode = subNode;

            //设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }

    }

    /**
     * 过滤敏感词,算法的核心逻辑来了！！
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果（变长字符串？？）
        StringBuilder sb = new StringBuilder();


        // 指针2只能向后移，指针3和指针1配合指针2，可能反复横跳

        while (position < text.length()) {
            char c = text.charAt(position);

            // 如果当前position指向符号
            if (isSymbol(c)) {
                // 若指针1处于根节点，将此符号计入结果，让指针2后移一位
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin ++;
                }
                // 指针1不在跟节点，说明符号在中间，指针2不动（不确定是否为敏感词，有可能是符号迷惑！）  其实指针2只有在确定为非敏感词时才会移动，当指针2移动时，指针3会跟进的同时，指针1回到根节点！！
                //无论符号在开头或中间，指针3后移一位
                position ++;
                continue;
            }

            // 检查下级节点(指针1移动)
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) { //不是敏感词,可以记录信息，并移动指针2，
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin; //begin先自加
                // 重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) { //发现敏感词
                // 替换
                sb.append(REPLACEMENT);
                // 指针移动
                begin = ++position;
                tempNode = rootNode;

            } else { //字典树继续向下搜索
                // 指针移动
                position ++;

            }
        }
        // 指针2有可能没有走到尽头（而只有指针2完全遍历，才能保证结果是完整的【每次需要移动指针2时才会sb.append()】）

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }



    // 前缀树
    private class TrieNode {


        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点（key是下级字符，value是下级节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点的方法
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点的方法
        public TrieNode getSubNode(Character c) {
            return this.subNodes.get(c);
        }
    }

}
