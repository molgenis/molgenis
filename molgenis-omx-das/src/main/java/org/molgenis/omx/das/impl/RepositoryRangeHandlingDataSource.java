package org.molgenis.omx.das.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.das.RangeHandlingDataSource;
import org.molgenis.util.ApplicationContextProvider;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;

public class RepositoryRangeHandlingDataSource extends RangeHandlingDataSource implements
		RangeHandlingAnnotationDataSource
{
	private final DataService dataService;
	private DasMethod method;

	@Override
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig) throws DataSourceException
	{
		this.method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
	}

	public RepositoryRangeHandlingDataSource() throws DataSourceException
	{
		dataService = ApplicationContextProvider.getApplicationContext().getBean(DataService.class);
	}

	@Override
	public DasAnnotatedSegment getFeatures(String segmentParamString, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException
	{

		String[] segmentParts = segmentParamString.split(",");
		String dataSet = null;
		String customParam;
		String segmentId = null;
		if (segmentParts.length > 1)
		{
			segmentId = segmentParts[0];
			customParam = segmentParts[1];
			if (customParam.indexOf("dataset_") != -1)
			{
				dataSet = customParam.substring(8);
			}
		}
		if (maxbins == null || maxbins < 0)
		{
			maxbins = 1000;
		}
		Iterable<Entity> entityIterable = queryDataSet(segmentId, dataSet, maxbins);
		List<DasFeature> features = new ArrayList<DasFeature>();

        Integer score = 0;
        Map<String, DasType> patients = new HashMap<String, DasType>();
		for (Entity entity : entityIterable)
		{
            DasFeature feature;

			Integer valueStart = null;
			Integer valueStop = null;
			String valueDescription = null;
			String valueIdentifier = null;
			String valueName = null;
			String valueLink = null;
            String valuePatient = null;
			try
			{
				valueStart = entity.getInt(MUTATION_START_POSITION);
				valueIdentifier = entity.getString(MUTATION_ID);
			}
			catch (ClassCastException e)
			{
				// start of identifier not correctly specified? exclude this mutation fore it can not be plotted
				break;
			}
			// no end position? assume mutation of 1 position, so stop == start
			valueStop = entity.getInt(MUTATION_STOP_POSITION) == null ? valueStart : entity
					.getInt(MUTATION_STOP_POSITION);
			valueDescription = entity.getString(MUTATION_DESCRIPTION) == null ? "" : entity
					.getString(MUTATION_DESCRIPTION);
			valueName = entity.getString(MUTATION_NAME) == null ? "" : entity.getString(MUTATION_NAME);
			valueLink = entity.getString(MUTATION_LINK) == null ? "" : entity.getString(MUTATION_LINK);
            valuePatient = entity.getString(PATIENT_ID) == null ? "" : entity.getString(PATIENT_ID);

			if ((valueStart >= start && valueStart <= stop) || (valueStop >= start && valueStop <= stop))
			{
                DasType type;//used for label colours in Dalliance
                if(patients.containsKey(valuePatient)){
                    type = patients.get(valuePatient);
                }else{
                    type = new DasType(score.toString(), "", "", "");
                    patients.put(valuePatient, type);
                    ++score;
                }

                feature = createDasFeature(valueStart, valueStop, valueIdentifier, valueName, valueDescription,
						valueLink, type, method, dataSet, valuePatient);
				features.add(feature);
            }
        }
		DasAnnotatedSegment segment = new DasAnnotatedSegment(segmentId, start, stop, "1.00", segmentId, features);
		return segment;
	}

	protected Iterable<Entity> queryDataSet(String segmentId, String dataSet, int maxbins)
	{
		Query q = new QueryImpl().eq(MUTATION_CHROMOSOME, segmentId);
		q.pageSize(maxbins);
		return dataService.findAll(dataSet, q);
	}

	protected DasFeature createDasFeature(Integer start, Integer stop, String identifier, String name,
			String description, String link, DasType type, String dataSet, Double score) throws DataSourceException
	{
		// create description based on available information
		String featureDescription = "";
		if (StringUtils.isNotEmpty(description))
		{
			featureDescription = StringUtils.isNotEmpty(name) ? name + "," + description : description;
		}
		else
		{
			featureDescription = identifier;
		}

		List<String> notes = new ArrayList<String>();

		Map<URL, String> linkout = new HashMap<URL, String>();
		try
		{
			linkout.put(new URL(link), "Link");
		}
		catch (MalformedURLException e)
		{
		}

		List<DasTarget> dasTarget = new ArrayList<DasTarget>();
		dasTarget.add(new MolgenisDasTarget(identifier, start, stop, featureDescription));

		List<String> parents = new ArrayList<String>();
		DasFeature feature = new DasFeature(identifier, featureDescription, type, method, start, stop,
				score, DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, notes,
				linkout, dasTarget, parents, null);
		return feature;
	}

	@Override
	public Integer getTotalCountForType(DasType type) throws DataSourceException
	{
		return 1000;
	}

	@Override
	public Collection<DasType> getTypes() throws DataSourceException
	{
		return Collections.emptyList();
	}
}
