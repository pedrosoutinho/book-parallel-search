import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ParallelCPUSearch implements WordSearch {

    private final int numThreads;

    public ParallelCPUSearch(int numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public int countOccurrences(String filePath, String word) throws IOException {
        List<String> lines = readFileIntoLines(filePath);

        ForkJoinPool forkJoinPool = new ForkJoinPool(numThreads);
        LineProcessorTask task = new LineProcessorTask(lines, word, 0, lines.size());

        return forkJoinPool.invoke(task);
    }

    private List<String> readFileIntoLines(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static class LineProcessorTask extends RecursiveTask<Integer> {
        private final List<String> lines;
        private final String word;
        private final int start;
        private final int end;

        private static final int THRESHOLD = 10; // Number of lines to process in a single task

        public LineProcessorTask(List<String> lines, String word, int start, int end) {
            this.lines = lines;
            this.word = word;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            if (end - start <= THRESHOLD) {
                // Process lines sequentially
                return countOccurrencesInLines(lines.subList(start, end), word);
            } else {
                // Split task
                int mid = start + (end - start) / 2;
                LineProcessorTask leftTask = new LineProcessorTask(lines, word, start, mid);
                LineProcessorTask rightTask = new LineProcessorTask(lines, word, mid, end);

                leftTask.fork(); // Execute the left task asynchronously
                int rightResult = rightTask.compute(); // Compute the right task in the current thread
                int leftResult = leftTask.join(); // Wait for the left task to complete

                return leftResult + rightResult;
            }
        }

        private int countOccurrencesInLines(List<String> lines, String word) {
            int count = 0;
            for (String line : lines) {
                int index = 0;
                while ((index = line.indexOf(word, index)) != -1) {
                    count++;
                    index += word.length();
                }
            }
            return count;
        }
    }
}

