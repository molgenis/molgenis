package org.molgenis.charts;

import org.molgenis.charts.AbstractChart.MolgenisChartType;
import org.molgenis.charts.highcharts.convert.HighchartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Here all the visualizationservices are registered.
 * <p>
 * You can ask a visualization service for a particular charttype
 */
@Component
public class ChartVisualizationServiceFactory
{
	private final List<ChartVisualizationService> chartVisualiationServices;

	@Autowired
	public ChartVisualizationServiceFactory(HighchartService highchartsService)
	{
		chartVisualiationServices = new ArrayList<>();
		chartVisualiationServices.add(highchartsService);
	}

	/**
	 * Gets a visualizationservice for a charttype
	 * <p>
	 * Throws MolgenisChartException if no service can be found for this charttype
	 *
	 * @param chartType
	 * @return
	 */
	public ChartVisualizationService getVisualizationService(MolgenisChartType chartType)
	{
		for (ChartVisualizationService service : chartVisualiationServices)
		{
			if (service.getCapabilities().contains(chartType))
			{
				return service;
			}
		}

		throw new MolgenisChartException("No service found for charttype [" + chartType + "]");
	}
}
