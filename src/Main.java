import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.BasicStroke;


import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    String csvFile = "resultados.csv";
    String filePaths[] = { "livros/DonQuixote.txt", "livros/MobyDick.txt", "livros/Dracula.txt" };

    void run(String word) {
        try {
            WordSearch naiveSearch = new NaiveSerialSearch();
            WordSearch parallelSearch = new ParallelCPUSearch(10);
            for (String filePath : filePaths) {
                for (int i = 0; i < 10; ++i) {
                    long startTime = System.nanoTime();
                    int naiveCount = naiveSearch.countOccurrences(filePath, word);
                    long endTime = System.nanoTime();

                    try (FileWriter writer = new FileWriter(csvFile, true)) {
                        writer.append("Sequencial naive," + filePath + "," + word + "," + naiveCount + ","
                                + (endTime - startTime) / 1e6 + "\n");
                    }

                    startTime = System.nanoTime();
                    int parallelCount = parallelSearch.countOccurrences(filePath, word);
                    endTime = System.nanoTime();

                    try (FileWriter writer = new FileWriter(csvFile, true)) {
                        writer.append("CPU Paralelo," + filePath + "," + word + "," + parallelCount + ","
                                + (endTime - startTime) / 1e6 + "\n");
                    }

                    assert (naiveCount == parallelCount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            reader.readLine();

            int samplePoint = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String algorithm = parts[0];
                double time = Double.parseDouble(parts[4]);

                System.out.println("Algorithm: " + algorithm + ", Sample: " + (samplePoint + 1) + ", Time: " + time);

                dataset.addValue(time, algorithm, "Sample " + (++samplePoint));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Comparação de algoritmos",
                "Amostras",
                "Tempo de execução (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        CategoryPlot plot = lineChart.getCategoryPlot();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        plot.setRenderer(renderer);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(lineChart));
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try (FileWriter writer = new FileWriter("resultados.csv")) {
            writer.append("Algoritmo,File,Word,Count,Time\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] words = { "the", "a", "and", "of", "to", "in", "he", "was", "that", "it", "Don", "Dracula", "Moby" };

        Main main = new Main();
        for (String word : words) {
            main.run(word);
        }

        main.generateChart();
    }
}
