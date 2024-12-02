import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class NaiveSerialSearch implements WordSearch {

    @Override
    public int countOccurrences(String filePath, String word) throws IOException {
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                count += countWordInLine(line, word);
            }
        }

        return count;
    }

    private int countWordInLine(String line, String word) {
        int count = 0;
        int index = 0;
        while ((index = line.indexOf(word, index)) != -1) {
            count++;
            index += word.length();
        }
        return count;
    }
}

