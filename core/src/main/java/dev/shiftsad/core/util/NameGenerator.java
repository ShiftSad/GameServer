package dev.shiftsad.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

public final class NameGenerator {

    private static final URL ADJECTIVES = NameGenerator.class.getResource("adjectives.txt");
    private static final URL ANIMALS = NameGenerator.class.getResource("animals.txt");
    private static final URL COLORS = NameGenerator.class.getResource("colors.txt");

    private static final List<String> adjectives = new ArrayList<>();
    private static final List<String> animals = new ArrayList<>();
    private static final List<String> colors = new ArrayList<>();
    private static final Random random = new Random();

    public enum WordType {
        ADJECTIVE,
        ANIMALS,
        COLORS
    }

    static {
        loadWordList(ADJECTIVES, adjectives);
        loadWordList(ANIMALS, animals);
        loadWordList(COLORS, colors);
    }

    private NameGenerator() {
        throw new UnsupportedOperationException(
                "NameGenerator is a utility class and should not be instantiated"
        );
    }

    private static void loadWordList(URL resourceUrl, List<String> wordList) {
        if (resourceUrl == null) {
            System.err.println("Resource file not found");
            return;
        }

        try (InputStream inputStream = resourceUrl.openStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream)
             )) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    wordList.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading resource file: " + e.getMessage());
        }
    }

    /**
     * Generates a random name using the specified word types, separated by -.
     *
     * @param wordTypes vararg of WordType enums specifying the pattern
     * @return a randomly generated name with words separated by -
     */
    public static String randomName(WordType... wordTypes) {
        if (wordTypes.length == 0) {
            return "Empty";
        }

        StringJoiner joiner = new StringJoiner("-");

        for (WordType wordType : wordTypes) {
            String word = getRandomWordByType(wordType);
            joiner.add(capitalizeFirst(word));
        }

        return joiner.toString();
    }

    /**
     * Gets a random word based on the specified word type.
     *
     * @param wordType the type of word to retrieve
     * @return a random word of the specified type
     */
    private static String getRandomWordByType(WordType wordType) {
        return switch (wordType) {
            case ADJECTIVE -> adjectives.isEmpty() ? "desconhecido" : getRandomWord(adjectives);
            case ANIMALS -> animals.isEmpty() ? "criatura" : getRandomWord(animals);
            case COLORS -> colors.isEmpty() ? "victor" : getRandomWord(colors);
        };
    }

    /**
     * Gets a random word from the specified list.
     *
     * @param wordList the list to select from
     * @return a randomly selected word
     */
    private static String getRandomWord(List<String> wordList) {
        return wordList.get(random.nextInt(wordList.size()));
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param word the word to capitalize
     * @return the word with the first letter capitalized
     */
    private static String capitalizeFirst(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}
