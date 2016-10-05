package org.molgenis.das.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.das.RangeHandlingDataSource;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.GenomicDataSettings;
import org.molgenis.data.support.QueryImpl;
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasType;

import javax.servlet.ServletContext;
import java.util.*;
import java.util.stream.Stream;

import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

public class RepositoryRangeHandlingDataSource extends RangeHandlingDataSource
		implements RangeHandlingAnnotationDataSource
{
	private final DataService dataService;
	private final GenomicDataSettings config;
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
		config = getApplicationContext().getBean(GenomicDataSettings.class);
	}

	@Override
	public DasAnnotatedSegment getFeatures(String segmentParamString, int start, int stop, Integer maxbins)
			throws BadReferenceObjectException, DataSourceException
	{
		String[] segmentParts = segmentParamString.split(",");
		String dataSet = null;
		String customParam;
		String segmentId = null;

		String posAttribute = null;
		String chromosomeAttribute = null;
		String stopAttribute = null;
		String refAttribute = null;
		String altAttribute = null;
		String descriptionAttribute = null;
		String linkAttribute = null;
		String patientAttribute = null;

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
		Stream<Entity> entityIterable = queryDataSet(segmentId, dataSet, maxbins);
		List<DasFeature> features = new ArrayList<DasFeature>();

		Integer score = 0;
		Map<String, DasType> patients = new HashMap<String, DasType>();
		for (Iterator<Entity> it = entityIterable.iterator(); it.hasNext(); )
		{
			Entity entity = it.next();

			DasFeature feature;

			Integer valueStart = null;
			Integer valueStop = null;
			String valueDescription = null;
			String valueLink = null;
			String valuePatient = null;
			String valueRef = null;
			String valueAlt = null;

			posAttribute = getAttributeName(posAttribute, GenomicDataSettings.Meta.ATTRS_POS, entity);
			chromosomeAttribute = getAttributeName(chromosomeAttribute, GenomicDataSettings.Meta.ATTRS_CHROM, entity);
			stopAttribute = getAttributeName(stopAttribute, GenomicDataSettings.Meta.ATTRS_STOP, entity);
			descriptionAttribute = getAttributeName(descriptionAttribute, GenomicDataSettings.Meta.ATTRS_DESCRIPTION,
					entity);
			refAttribute = getAttributeName(refAttribute, GenomicDataSettings.Meta.ATTRS_REF, entity);
			altAttribute = getAttributeName(altAttribute, GenomicDataSettings.Meta.ATTRS_ALT, entity);
			linkAttribute = getAttributeName(linkAttribute, GenomicDataSettings.Meta.ATTRS_LINKOUT, entity);
			patientAttribute = getAttributeName(patientAttribute, GenomicDataSettings.Meta.ATTRS_PATIENT_ID, entity);

			try
			{
				valueStart = entity.getInt(posAttribute);
			}
			catch (ClassCastException e)
			{
				// start of identifier not correctly specified? exclude this mutation fore it can not be plotted
				break;
			}
			// no end position? assume mutation of 1 position, so stop == start
			List<String> attributes = Lists.newArrayList(entity.getAttributeNames().iterator());
			valueStop = Iterables.contains(attributes, stopAttribute) ? entity.getInt(stopAttribute) : valueStart;
			valueDescription = Iterables.contains(attributes, descriptionAttribute) ? entity
					.getString(descriptionAttribute) : "";
			valueLink = Iterables.contains(attributes, linkAttribute) ? entity.getString(linkAttribute) : "";
			valuePatient = Iterables.contains(attributes, patientAttribute) ? entity.getString(patientAttribute) : "";

			valueRef = StringUtils.isNotEmpty(refAttribute) && StringUtils
					.isNotEmpty(entity.getString(refAttribute)) ? entity.getString(refAttribute) : "";
			valueAlt = StringUtils.isNotEmpty(altAttribute) && StringUtils
					.isNotEmpty(entity.getString(altAttribute)) ? entity.getString(altAttribute) : "";

			List<String> notes = new ArrayList<String>();
			if (StringUtils.isNotEmpty(valueRef)) notes.add(refAttribute + "~" + valueRef);
			if (StringUtils.isNotEmpty(valueAlt)) notes.add(altAttribute + "~" + valueAlt);

			if (valueStart != null && ((valueStart >= start && valueStart <= stop) || (valueStop >= start
					&& valueStop <= stop)))
			{
				DasType type;// used for label colours in Dalliance
				if (!StringUtils.isEmpty(valueRef) && !StringUtils.isEmpty(valueAlt))
				{
					if (valueRef.length() == 1 && valueAlt.length() == 1) type = new DasType(valueAlt, "", "", "");
					else if (valueRef.length() == 1 && valueAlt.length() > 1)
					{
						type = new DasType("insert", "", "", "");
					}
					else if (valueRef.length() > 1 && valueAlt.length() == 1)
					{
						type = new DasType("delete", "", "", "");
					}
					else
					{
						type = new DasType("delete", "", "", "");
					}
				}
				else if (patients.containsKey(valuePatient))
				{
					type = patients.get(valuePatient);
				}
				else
				{
					type = new DasType(score.toString(), "", "", "");
					patients.put(valuePatient, type);
					++score;
				}

				Object labelValue = entity.getLabelValue();
				feature = createDasFeature(valueStart, valueStop, entity.getIdValue().toString(),
						labelValue != null ? labelValue.toString() : null, valueDescription, valueLink, type, method,
						dataSet, valuePatient, notes);
				features.add(feature);
			}
		}
		DasAnnotatedSegment segment = new DasAnnotatedSegment(segmentId, start, stop, "1.00", segmentId, features);
		return segment;
	}

	public String getAttributeName(String attribute, String fieldName, Entity entity)
	{
		if (attribute == null)
		{
			attribute = config.getAttributeNameForAttributeNameArray(fieldName, entity.getEntityMetaData());
		}
		return attribute;
	}

	protected Stream<Entity> queryDataSet(String segmentId, String dataSet, int maxbins)
	{

		String chromosomeAttribute = config.getAttributeNameForAttributeNameArray(GenomicDataSettings.Meta.ATTRS_CHROM,
				dataService.getEntityMetaData(dataSet));
		Query<Entity> q = new QueryImpl<Entity>().eq(chromosomeAttribute, segmentId);
		q.pageSize(maxbins);
		return dataService.findAll(dataSet, q);
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
