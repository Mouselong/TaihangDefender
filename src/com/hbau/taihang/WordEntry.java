package com.hbau.taihang;

public class WordEntry {
    public String englishWord;     // 只存英文，如 "agriculture"
    public String chineseMeaning;  // 只存中文，如 "农业"

    public WordEntry(String englishWord, String chineseMeaning) {
        this.englishWord = englishWord;
        this.chineseMeaning = chineseMeaning;
    }

    public String getEnglishWord() { return englishWord; }
    public String getChineseMeaning() { return chineseMeaning; }

    // 保持向后兼容的方法
    public String getEnglish() { return englishWord; }
    public String getChinese() { return chineseMeaning; }

    @Override
    public String toString() {
        return englishWord + " (" + chineseMeaning + ")";
    }
}
