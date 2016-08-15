package org.molgenis.das;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasType;

import javax.servlet.ServletContext;
import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: charbonb
 * Date: 16/01/14
 * Time: 09:29
 * To change this template use File | Settings | File Templates.
 */
public class TestDataSource extends RangeHandlingDataSource
{
	@Override
	public DasAnnotatedSegment getFeatures(String segmentId, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, CoordinateErrorException, DataSourceException
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig) throws DataSourceException
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Collection<DasType> getTypes() throws DataSourceException
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public Integer getTotalCountForType(DasType type) throws DataSourceException
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
