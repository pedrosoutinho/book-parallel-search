public class Main {
    public static void main(String[] args) {
        String filePath = "livros/DonQuixote-388208.txt";
        String word = "this";

        try {
            WordSearch naiveSearch = new NaiveSerialSearch();
            int naiveCount = naiveSearch.countOccurrences(filePath, word);
            System.out.println("Naive Serial Search Count: " + naiveCount);

            WordSearch parallelSearch = new ParallelCPUSearch(4);
            int parallelCount = parallelSearch.countOccurrences(filePath, word);
            System.out.println("Parallel CPU Search Count: " + parallelCount);

            WordSearch gpuSearch = new ParallelGPUSearch();
            int gpuCount = gpuSearch.countOccurrences(filePath, word);
            System.out.println("Parallel GPU Search Count: " + gpuCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
