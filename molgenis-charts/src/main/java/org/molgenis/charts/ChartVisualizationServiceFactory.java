package org.molgenis.charts;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.charts.AbstractChart.AbstractChartType;
import org.molgenis.charts.r.RChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Here all the visualizationservices are registered.
 * 
 * You can ask a visualization service for a particular charttype
 */
@Component
public class ChartVisualizationServiceFactory
{
	private final List<ChartVisualizationService> chartVisualiationServices;

	@Autowired
	public ChartVisualizationServiceFactory(RChartService rChartService)
	{
		chartVisualiationServices = new ArrayList<ChartVisualizationService>();
		chartVisualiationServices.add(rChartService);
	}

	/**
	 * Gets a visualizationservice for a charttype
	 * 
	 * Throws MolgenisChartException if no service can be found for this charttype
	 * 
	 * @param chartType
	 * @return
	 */
	public ChartVisualizationService getVisualizationService(AbstractChartType abstractChartType)
	{
		for (ChartVisualizationService service : chartVisualiationServices)
		{
			if (service.getCapabilities().contains(abstractChartType))
			{
				return service;
			}
		}

		throw new MolgenisChartException("No service found for charttype [" + abstractChartType + "]");
	}
}
