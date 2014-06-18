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

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.GenomeConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.server.MolgenisSettings;
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

import static org.molgenis.util.ApplicationContextProvider.*;

public class RepositoryRangeHandlingDataSource extends RangeHandlingDataSource implements
		RangeHandlingAnnotationDataSource
{
	private final DataService dataService;
	private final GenomeConfig config;
	private DasType mutationType;
	private DasMethod method;
	private String type;

	@Override
	public void init(ServletContext servletContext, Map<String, PropertyType> globalParameters,
			DataSourceConfiguration dataSourceConfig) throws DataSourceException
	{
		this.type = dataSourceConfig.getDataSourceProperties().get("type").getValue();
		this.mutationType = new DasType(type, null, "?", type);
		this.method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");
	}

	public RepositoryRangeHandlingDataSource() throws DataSourceException
	{
		dataService = getApplicationContext().getBean(DataService.class);
		config = getApplicationContext().getBean(GenomeConfig.class);
	}

	@Override
	public DasAnnotatedSegment getFeatures(String segmentParamString, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException
	{
		String[] segmentParts = segmentParamString.split(",");
		String dataSet = null;
		String customParam;
		String segmentId = null;

		String startAttribute = null;
		String chromosomeAttribute = null;
		String idAttribute = null;
		String stopAttribute = null;
		String descriptionAttribute = null;

		if (segmentParts.length > 1)
		{
			segmentId = segmentParts[0];
			customParam = segmentParts[1];
			if (customParam.indexOf(DasURLFilter.DATASET_PREFIX) != -1)
			{
				dataSet = customParam.substring(11);
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
			if (startAttribute == null)
			{
				startAttribute = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_START,
						entity.getEntityMetaData());
			}
			if (chromosomeAttribute == null)
			{
				chromosomeAttribute = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_CHROM,
						entity.getEntityMetaData());
			}
			if (idAttribute == null)
			{
				idAttribute = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_ID,
						entity.getEntityMetaData());
			}
			if (stopAttribute == null)
			{
				stopAttribute = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_STOP,
						entity.getEntityMetaData());
			}
			if (descriptionAttribute == null)
			{
				descriptionAttribute = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_DESCRIPTION,
						entity.getEntityMetaData());
			}

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
				valueStart = entity.getInt(startAttribute);
				valueIdentifier = entity.getString(idAttribute);
			}
			catch (ClassCastException e)
			{
				// start of identifier not correctly specified? exclude this mutation fore it can not be plotted
				break;
			}
			// no end position? assume mutation of 1 position, so stop == start
			Iterable<String> attributes = entity.getAttributeNames();

			valueStop = Iterables.contains(attributes, stopAttribute) ? entity.getInt(stopAttribute) : valueStart;

			valueDescription = Iterables.contains(attributes, descriptionAttribute) ? entity
					.getString(descriptionAttribute) : "";
			String name = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_NAME,
					entity.getEntityMetaData());
			String link = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_LINK,
					entity.getEntityMetaData());
			String patient = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_PATIENT_ID,
					entity.getEntityMetaData());
			valueName = Iterables.contains(attributes, name) ? entity.getString(name) : "";
			valueLink = Iterables.contains(attributes, link) ? entity.getString(link) : "";
			valuePatient = Iterables.contains(attributes, patient) ? entity.getString(patient) : "";

			if (valueStart != null
					&& ((valueStart >= start && valueStart <= stop) || (valueStop >= start && valueStop <= stop)))
			{
				DasType type;// used for label colours in Dalliance
				if (patients.containsKey(valuePatient))
				{
					type = patients.get(valuePatient);
				}
				else
				{
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
		String chromosomeAttribute = config.getAttributeNameForAttributeNameArray(config.GENOMEBROWSER_CHROM,
				dataService.getEntityMetaData(dataSet));
		Query q = new QueryImpl().eq(chromosomeAttribute, segmentId);
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
		DasFeature feature = new DasFeature(identifier, featureDescription, type, method, start, stop, score,
				DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE, notes, linkout,
				dasTarget, parents, null);
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
		return Collections.singleton(mutationType);
	}
}
