package com.hbau.taihang;

/**
 * Trie树（字典树），用于高效的单词前缀匹配和首字母锁定
 */
public class Trie {
    private final TrieNode root = new TrieNode();
    private TrieNode currentNode = root; // 当前输入路径位置，用于实时匹配
    private char lockedFirstChar = 0; // 首字母锁定

    public Trie() {
    }

    /**
     * 插入单词到Trie树
     */
    public void insert(WordEntry wordEntry) {
        if (wordEntry == null || wordEntry.getEnglishWord().isEmpty()) {
            return;
        }

        TrieNode node = root;
        String word = wordEntry.getEnglishWord(); // ONLY USE ENGLISH WORD
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!node.hasChild(c)) {
                node.addChild(c, new TrieNode());
            }
            node = node.getChild(c);
        }
        node.setEndOfWord(true);
        node.setWordEntry(wordEntry);
    }

    /**
     * 重新开始输入匹配，重置到根节点
     */
    public void resetInput() {
        currentNode = root;
        lockedFirstChar = 0;
    }

    /**
     * 输入一个字符，沿着Trie树走下去，支持首字母锁定
     * @param c 输入的字符
     * @return 是否是有效前缀
     */
    public boolean inputChar(char c) {
        // 首字母锁定逻辑
        if (currentNode == root) {
            if (lockedFirstChar == 0) {
                // 第一次输入，锁定首字母
                lockedFirstChar = c;
            } else if (lockedFirstChar != c) {
                // 首字母不匹配，不允许
                return false;
            }
        }

        if (currentNode.hasChild(c)) {
            currentNode = currentNode.getChild(c);
            return true;
        }
        return false;
    }

    /**
     * 检查当前路径是否是一个完整的单词
     * @return 是否是完整单词
     */
    public boolean isCompleteWord() {
        return currentNode.isEndOfWord();
    }

    /**
     * 获取当前匹配到的完整单词
     * @return WordEntry，或null
     */
    public WordEntry getCurrentWord() {
        if (currentNode.isEndOfWord()) {
            return currentNode.getWordEntry();
        }
        return null;
    }

    /**
     * 检查单词是否是前缀
     * @param prefix 前缀字符串
     * @return 是否是前缀
     */
    public boolean isPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }

        TrieNode node = root;
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (!node.hasChild(c)) {
                return false;
            }
            node = node.getChild(c);
        }
        return true;
    }

    /**
     * 检查单词是否存在（完整匹配）
     */
    public boolean contains(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }

        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!node.hasChild(c)) {
                return false;
            }
            node = node.getChild(c);
        }
        return node.isEndOfWord();
    }

    /**
     * 查找单词
     * @param word 要查找的单词
     * @return WordEntry，或null
     */
    public WordEntry find(String word) {
        if (word == null || word.isEmpty()) {
            return null;
        }

        TrieNode node = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (!node.hasChild(c)) {
                return null;
            }
            node = node.getChild(c);
        }
        return node.isEndOfWord() ? node.getWordEntry() : null;
    }

    /**
     * 计算前缀匹配长度
     */
    public int getPrefixMatchLength(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        TrieNode node = root;
        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!node.hasChild(c)) {
                break;
            }
            node = node.getChild(c);
            count++;
        }
        return count;
    }

    /**
     * 获取锁定的首字母
     */
    public char getLockedFirstChar() {
        return lockedFirstChar;
    }

    /**
     * 清除首字母锁定
     */
    public void clearLock() {
        lockedFirstChar = 0;
        currentNode = root;
    }
}
