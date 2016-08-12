package org.molgenis.charts;

import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.springframework.ui.Model;

import java.util.List;

public abstract class AbstractChartVisualizationService implements ChartVisualizationService
{
	private final List<MolgenisChartType> chartTypes;

	public AbstractChartVisualizationService(List<MolgenisChartType> chartTypes)
	{
		this.chartTypes = chartTypes;
	}

	@Override
	public List<MolgenisChartType> getCapabilities()
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
