package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.AbstractChart.ChartType;
import org.springframework.ui.Model;

public abstract class AbstractChartVisualizationService implements ChartVisualizationService
{
	private final List<ChartType> chartTypes;

	public AbstractChartVisualizationService(List<ChartType> chartTypes)
	{
		this.chartTypes = chartTypes;
	}

	@Override
	public List<ChartType> getCapabilities()
	{
		return this.chartTypes;
	}

	@Override
	public Object renderChart(AbstractChart chart, Model model)
	{
		if (!getCapabilities().contains(chart.getType()))
		{
			throw new MolgenisChartException("Charttype [" + chart.getType() + " not supported");
		}

		return renderChartInternal(chart, model);
	}

	protected abstract Object renderChartInternal(AbstractChart chart, Model model);

}
