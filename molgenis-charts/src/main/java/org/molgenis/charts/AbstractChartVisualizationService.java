package org.molgenis.charts;

import java.util.List;

import org.molgenis.charts.AbstractChart.AbstractChartType;
import org.springframework.ui.Model;

public abstract class AbstractChartVisualizationService implements ChartVisualizationService
{
	private final List<AbstractChartType> abstractChartTypes;

	public AbstractChartVisualizationService(List<AbstractChartType> abstractChartTypes)
	{
		this.abstractChartTypes = abstractChartTypes;
	}

	@Override
	public List<AbstractChartType> getCapabilities()
	{
		return this.abstractChartTypes;
	}

	@Override
	public String renderChart(AbstractChart chart, Model model)
	{
		if (!getCapabilities().contains(chart.getType()))
		{
			throw new MolgenisChartException("Charttype [" + chart.getType() + " not supported");
		}

		return renderChartInternal(chart, model);
	}

	protected abstract String renderChartInternal(AbstractChart chart, Model model);

}
