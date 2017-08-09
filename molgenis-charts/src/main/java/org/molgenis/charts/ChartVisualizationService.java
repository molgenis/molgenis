package org.molgenis.charts;

import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.springframework.ui.Model;

import java.util.List;

/**
 * Renders a chart.
 * <p>
 * Each visualization engine must implement this interface (for example r and hichart visualization services
 */
public interface ChartVisualizationService
{
	/**
	 * Gets the chart types this service can render
	 *
	 * @return
	 */
	List<MolgenisChartType> getCapabilities();

	/**
	 * Renders the chart
	 *
	 * @param chart , the chart to render
	 * @param model , the Spring controller model
	 * @return the name of the freemarker template
	 */
	Object renderChart(AbstractChart chart, Model model);
}
