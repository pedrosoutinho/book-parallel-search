import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelCPUSearch implements WordSearch {

    private final int numThreads;

    public ParallelCPUSearch(int numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public int countOccurrences(String filePath, String word) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<Integer>> futures = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String currentLine = line; // Required for lambda compatibility
                futures.add(executorService.submit(() -> countWordInLine(currentLine, word)));
            }
        }

        int total = 0;
        for (Future<Integer> future : futures) {
            try {
                total += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        return total;
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
