package com.hbau.taihang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Word manager: loads bilingual words from words/ and picks words at random.
 * Format: english,chinese
 * Uses Trie树 for efficient prefix matching.
 */
public class WordManager {
    private final List<WordEntry> allWords = new ArrayList<>();
    private final Trie trie = new Trie();
    private final Random random = new Random();
    private WordEntry currentWord = null;
    private String lastInput = "";

    public void loadAll() {
        // 1) Classpath: words/ (works when packaged into jar or when words/ is on classpath)
        loadClasspathResource("words/basic.txt");
        loadClasspathResource("words/professional.txt");
        loadClasspathResource("words/spirit.txt");
        loadClasspathResource("words/agriculture.txt");

        // 2) Try to locate words/ relative to the code source (class/jar location)
        if (allWords.isEmpty()) {
            try {
                URL codeLoc = getClass().getProtectionDomain().getCodeSource().getLocation();
                if (codeLoc != null) {
                    Path base = Paths.get(codeLoc.toURI());
                    // If it is a file (not jar), check sibling "words/" directory
                    if (Files.isDirectory(base)) {
                        Path wordsDir = base.resolve("words");
                        if (Files.isDirectory(wordsDir)) {
                            loadFromDir(wordsDir);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 3) Fallback: try known filesystem paths relative to CWD
        if (allWords.isEmpty()) {
            loadFile(new java.io.File("TaihangDefender/out/words/basic.txt"));
            loadFile(new java.io.File("TaihangDefender/out/words/professional.txt"));
            loadFile(new java.io.File("TaihangDefender/out/words/spirit.txt"));
            loadFile(new java.io.File("TaihangDefender/out/words/agriculture.txt"));
        }
        if (allWords.isEmpty()) {
            loadFile(new java.io.File("TaihangDefender/resources/words/basic.txt"));
            loadFile(new java.io.File("TaihangDefender/resources/words/professional.txt"));
            loadFile(new java.io.File("TaihangDefender/resources/words/spirit.txt"));
            loadFile(new java.io.File("TaihangDefender/resources/words/agriculture.txt"));
        }
        if (allWords.isEmpty()) {
            loadFile(new java.io.File("resources/words/basic.txt"));
            loadFile(new java.io.File("resources/words/professional.txt"));
            loadFile(new java.io.File("resources/words/spirit.txt"));
            loadFile(new java.io.File("resources/words/agriculture.txt"));
        }
        if (allWords.isEmpty()) {
            loadFile(new java.io.File("words/basic.txt"));
            loadFile(new java.io.File("words/professional.txt"));
            loadFile(new java.io.File("words/spirit.txt"));
            loadFile(new java.io.File("words/agriculture.txt"));
        }
        // 将所有单词添加到Trie树
        for (WordEntry entry : allWords) {
            trie.insert(entry);
        }
    }

    private void loadFromDir(Path dir) {
        loadFile(dir.resolve("basic.txt").toFile());
        loadFile(dir.resolve("professional.txt").toFile());
        loadFile(dir.resolve("spirit.txt").toFile());
        loadFile(dir.resolve("agriculture.txt").toFile());
    }

    private void loadClasspathResource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) return;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    parseLine(line);
                }
            }
        } catch (Exception ex) {
            // ignore classpath loading errors; fallback will handle
        }
    }

    private void loadFile(java.io.File f) {
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(f), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                parseLine(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseLine(String line) {
        line = line.trim();
        if (line.isEmpty()) return;
        if (line.startsWith("#")) return;

        if (line.contains(",")) {
            String[] parts = line.split(",");
            String englishWord = parts[0].trim();
            String chineseMeaning = "";
            if (parts.length > 1) {
                chineseMeaning = parts[1].trim();
            }
            allWords.add(new WordEntry(englishWord, chineseMeaning));
        } else {
            // Backward compatibility: single word without translation
            allWords.add(new WordEntry(line, ""));
        }
    }

    public void pickNextWord() {
        trie.resetInput(); // 重置Trie树状态
        if (allWords.isEmpty()) {
            currentWord = new WordEntry("apple", "苹果");
            return;
        }
        if (allWords.size() == 1) {
            currentWord = allWords.get(0);
            lastInput = "";
            return;
        }
        // Avoid picking the same word consecutively
        WordEntry prev = currentWord;
        WordEntry next;
        do {
            next = allWords.get(random.nextInt(allWords.size()));
        } while (prev != null && next.getEnglishWord().equals(prev.getEnglishWord()));
        currentWord = next;
        lastInput = "";
    }

    public String getCurrentWordEnglish() {
        return currentWord != null ? currentWord.getEnglishWord() : "apple";
    }

    public String getCurrentWordChinese() {
        return currentWord != null ? currentWord.getChineseMeaning() : "";
    }

    public WordEntry getCurrentWordEntry() {
        return currentWord;
    }

    public String getLastInput() { return lastInput; }

    public void updateInput(String input) { lastInput = input; }

    public void clearInput() { 
        lastInput = ""; 
        trie.resetInput();
    }

    public boolean matchesCurrentWord(String typed) {
        if (currentWord == null) return false;
        // Strict case-sensitive equality - ONLY USE ENGLISH WORD
        return typed.equals(currentWord.getEnglishWord());
    }

    public boolean isPrefix(String typed) {
        if (currentWord == null) return false;
        if (typed == null || typed.isEmpty()) return true;
        // ONLY USE ENGLISH WORD for prefix matching
        return currentWord.getEnglishWord().startsWith(typed);
    }

    public int getMatchedPrefixLength() {
        if (currentWord == null) return 0;
        String eng = currentWord.getEnglishWord(); // ONLY USE ENGLISH
        int max = Math.min(eng.length(), lastInput.length());
        int count = 0;
        for (int i = 0; i < max; i++) {
            char c1 = eng.charAt(i);
            char c2 = lastInput.charAt(i);
            if (c1 != c2) break;
            count++;
        }
        return count;
    }
    
    /**
     * 获取Trie树引用，用于更高效的前缀匹配
     */
    public Trie getTrie() {
        return trie;
    }
}
