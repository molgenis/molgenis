package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.HgncLocationsUtils;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component("CgdService")
public class ClinicalGenomicsDatabaseServiceAnnotator extends LocusAnnotator
{

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;
	private final HgncLocationsProvider hgncLocationsProvider;

	private static final String NAME = "Clinical Genomic Database";
	public static final String CGD_FILE_LOCATION_PROPERTY = "cgd_location";

	public static final String REFERENCES = "REFERENCES";
	public static final String INTERVENTION_RATIONALE = "INTERVENTION/RATIONALE";
	public static final String COMMENTS = "COMMENTS";
	public static final String INTERVENTION_CATEGORIES = "INTERVENTION CATEGORIES";
	public static final String MANIFESTATION_CATEGORIES = "MANIFESTATION CATEGORIES";
	public static final String ALLELIC_CONDITIONS = "ALLELIC CONDITIONS";
	public static final String AGE_GROUP = "AGE GROUP";
	public static final String INHERITANCE = "INHERITANCE";
	public static final String CONDITION = "CONDITION";
	public static final String ENTREZ_GENE_ID = "ENTREZ GENE ID";
	public static final String GENE = "GENE";
	public static final String HGNC_ID = "HGNC ID";

	@Autowired
	public ClinicalGenomicsDatabaseServiceAnnotator(MolgenisSettings molgenisSettings,
			AnnotationService annotationService, HgncLocationsProvider hgncLocationsProvider) throws IOException
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		if (annotationService == null) throw new IllegalArgumentException("annotationService is null");
		if (hgncLocationsProvider == null) throw new IllegalArgumentException("hgncLocationsProvider is null");
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotationService;
		this.hgncLocationsProvider = hgncLocationsProvider;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public boolean annotationDataExists()
	{
		return new File(molgenisSettings.getProperty(CGD_FILE_LOCATION_PROPERTY)).exists();
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException
	{
		List<Entity> results = new ArrayList<Entity>();

		Long position = entity.getLong(POSITION);
		String chromosome = entity.getString(CHROMOSOME);

		List<String> geneSymbols = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(),
				new Locus(chromosome, position));

		try
		{
			if (!isAllNulls(geneSymbols))
			{
				List<String> fileLines = IOUtils.readLines(new InputStreamReader(new FileInputStream(new File(
						molgenisSettings.getProperty(CGD_FILE_LOCATION_PROPERTY))), "UTF-8"));

				for (String line : fileLines)
				{
					if (!line.startsWith("#"))
					{
						String[] split = line.split("\t");
						for (String gene : geneSymbols)
						{
							if (gene.equals(split[0]))
							{
								HashMap<String, Object> resultMap = new HashMap<String, Object>();
								resultMap.put(GENE, split[0]);
								resultMap.put(HGNC_ID, split[1]);
								resultMap.put(ENTREZ_GENE_ID, split[2]);
								resultMap.put(CONDITION, split[3]);
								resultMap.put(INHERITANCE, split[4]);
								resultMap.put(AGE_GROUP, split[5]);
								resultMap.put(ALLELIC_CONDITIONS, split[6]);
								resultMap.put(MANIFESTATION_CATEGORIES, split[7]);
								resultMap.put(INTERVENTION_CATEGORIES, split[8]);
								resultMap.put(COMMENTS, split[9]);
								resultMap.put(INTERVENTION_RATIONALE, split[10]);
								resultMap.put(REFERENCES, split[11]);
								resultMap.put(CHROMOSOME, chromosome);
								resultMap.put(POSITION, position);

								results.add(new MapEntity(resultMap));
							}
						}
					}
				}
			}
			else
			{
				HashMap<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(CHROMOSOME, chromosome);
				resultMap.put(POSITION, position);
				results.add(new MapEntity(resultMap));
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results;
	}

	private boolean isAllNulls(Iterable<?> array)
	{
		for (Object element : array)
			if (element != null) return false;
		return true;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GENE, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HGNC_ID, MolgenisFieldTypes.FieldTypeEnum.LONG));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ENTREZ_GENE_ID, MolgenisFieldTypes.FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CONDITION, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INHERITANCE, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(AGE_GROUP, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ALLELIC_CONDITIONS,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MANIFESTATION_CATEGORIES,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INTERVENTION_CATEGORIES,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(COMMENTS, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INTERVENTION_RATIONALE,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(REFERENCES, MolgenisFieldTypes.FieldTypeEnum.STRING));

		return metadata;
	}
}
