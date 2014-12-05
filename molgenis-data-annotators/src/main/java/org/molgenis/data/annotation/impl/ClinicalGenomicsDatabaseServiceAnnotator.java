package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.HgncLocationsUtils;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.impl.datastructures.CgdData;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.provider.CgdDataProvider;
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
	private final CgdDataProvider cgdDataProvider;

	private static final String NAME = "ClinicalGenomicsDatabase";

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
			AnnotationService annotationService, HgncLocationsProvider hgncLocationsProvider,
			CgdDataProvider cgdDataProvider) throws IOException
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		if (annotationService == null) throw new IllegalArgumentException("annotationService is null");
		if (hgncLocationsProvider == null) throw new IllegalArgumentException("hgncLocationsProvider is null");
		if (cgdDataProvider == null) throw new IllegalArgumentException("cgdData is null");
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotationService;
		this.hgncLocationsProvider = hgncLocationsProvider;
		this.cgdDataProvider = cgdDataProvider;
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
		return new File(molgenisSettings.getProperty(CgdDataProvider.CGD_FILE_LOCATION_PROPERTY)).exists();
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException
	{

		List<Entity> results = new ArrayList<Entity>();

		Long position = entity.getLong(POSITION);
		String chromosome = entity.getString(CHROMOSOME);

		String geneSymbol = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(),
				new Locus(chromosome, position)).get(0);

		Map<String, CgdData> cgdData = cgdDataProvider.getCgdData();

		try
		{
			HashMap<String, Object> resultMap = new HashMap<String, Object>();

			if (cgdData.containsKey(geneSymbol))
			{
				CgdData data = cgdData.get(geneSymbol);

				resultMap.put(GENE, geneSymbol);
				resultMap.put(HGNC_ID, data.getHgnc_id());
				resultMap.put(ENTREZ_GENE_ID, data.getEntrez_gene_id());
				resultMap.put(CONDITION, data.getCondition());
				resultMap.put(INHERITANCE, data.getInheritance());
				resultMap.put(AGE_GROUP, data.getAge_group());
				resultMap.put(ALLELIC_CONDITIONS, data.getAllelic_conditions());
				resultMap.put(MANIFESTATION_CATEGORIES, data.getManifestation_categories());
				resultMap.put(INTERVENTION_CATEGORIES, data.getIntervention_categories());
				resultMap.put(COMMENTS, data.getComments());
				resultMap.put(INTERVENTION_RATIONALE, data.getIntervention_rationale());
				resultMap.put(REFERENCES, data.getReferences());
				resultMap.put(CHROMOSOME, chromosome);
				resultMap.put(POSITION, position);

				results.add(getAnnotatedEntity(entity, resultMap));
			}
			else
			{
				results.add(getAnnotatedEntity(entity, resultMap));
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GENE, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HGNC_ID, MolgenisFieldTypes.FieldTypeEnum.LONG));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ENTREZ_GENE_ID, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CONDITION, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INHERITANCE, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(AGE_GROUP, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ALLELIC_CONDITIONS,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MANIFESTATION_CATEGORIES,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INTERVENTION_CATEGORIES,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(COMMENTS, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(INTERVENTION_RATIONALE,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(REFERENCES, MolgenisFieldTypes.FieldTypeEnum.TEXT));

		return metadata;
	}
}
