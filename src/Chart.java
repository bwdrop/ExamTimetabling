import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;

/**
 * Created by Héliane Ly on 09/08/2016.
 */
public class Chart {
    private DefaultCategoryDataset lineChartDataset = new DefaultCategoryDataset();

    public Chart() {}

    public void addValue(int value, int iteration) {
        lineChartDataset.addValue((Number) value, "fitness", iteration);
    }

    public int export(String filename) {
        try {
            JFreeChart lineChartObj = ChartFactory.createLineChart(
                    "Best fitness vs iteration",
                    "Iterations",
                    "Best fitness",
                    lineChartDataset, PlotOrientation.VERTICAL,
                    true, true, false);
            File lineChart = new File(filename);
            ChartUtilities.saveChartAsJPEG(lineChart, lineChartObj, 1024, 768);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
