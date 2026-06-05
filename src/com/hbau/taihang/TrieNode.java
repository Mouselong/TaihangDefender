package com.hbau.taihang;

import java.util.HashMap;
import java.util.Map;

/**
 * Trie树节点，用于高效的单词前缀匹配
 */
public class TrieNode {
    private final Map<Character, TrieNode> children = new HashMap<>();
    private boolean isEndOfWord = false;
    private WordEntry wordEntry = null;

    public TrieNode() {
    }

    public boolean hasChild(char c) {
        return children.containsKey(c);
    }

    public TrieNode getChild(char c) {
        return children.get(c);
    }

    public void addChild(char c, TrieNode node) {
        children.put(c, node);
    }

    public boolean isEndOfWord() {
        return isEndOfWord;
    }

    public void setEndOfWord(boolean endOfWord) {
        isEndOfWord = endOfWord;
    }

    public WordEntry getWordEntry() {
        return wordEntry;
    }

    public void setWordEntry(WordEntry wordEntry) {
        this.wordEntry = wordEntry;
    }

    public Map<Character, TrieNode> getChildren() {
        return children;
    }
}
