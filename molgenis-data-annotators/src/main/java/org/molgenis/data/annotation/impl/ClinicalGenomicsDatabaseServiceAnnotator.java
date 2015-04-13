package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.AnnotatorUtils;
import org.molgenis.data.annotation.HgncLocationsUtils;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.annotation.impl.datastructures.CgdData;
import org.molgenis.data.annotation.impl.datastructures.Locus;
import org.molgenis.data.annotation.provider.CgdDataProvider;
import org.molgenis.data.annotation.provider.HgncLocationsProvider;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component("CgdService")
public class ClinicalGenomicsDatabaseServiceAnnotator extends LocusAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(ClinicalGenomicsDatabaseServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;
	private final HgncLocationsProvider hgncLocationsProvider;
	private final CgdDataProvider cgdDataProvider;

	private static final String NAME = "ClinicalGenomicsDatabase";
	private boolean runningFromCommandLine = false;

	public static final String REFERENCES = "REFERENCES";
	public static final String INTERVENTION_RATIONALE = "INTERVENTION/RATIONALE";
	public static final String COMMENTS = "COMMENTS";
	public static final String INTERVENTION_CATEGORIES = "INTERVENTION CATEGORIES";
	public static final String MANIFESTATION_CATEGORIES = "MANIFESTATION CATEGORIES";
	public static final String ALLELIC_CONDITIONS = "ALLELIC CONDITIONS";
	public static final String ENTREZ_GENE_ID = "ENTREZ GENE ID";
	public static final String GENE = "GENE";
	public static final String HGNC_ID = "HGNC ID";

	public static final String CGD_FILE_LOCATION = "cgd_location";
    // FIXME for the commandline VCF output we have to use different names for conciseness/uniqueness..
	public static final String CGD_CONDITION = "CGDCOND";
	public static final String CGD_AGE_GROUP = "CGDAGE";
	public static final String CGD_INHERITANCE = "CGDINH";
	public static final String CGD_GENERALIZED_INHERITANCE = "CGDGIN";

	final List<String> infoFields = Arrays.asList(new String[]
	{
			"##INFO=<ID=" + CGD_CONDITION.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=String,Description=\"CGD_CONDITION\">",
			"##INFO=<ID=" + CGD_AGE_GROUP.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=String,Description=\"CGD_AGE_GROUP\">",
			"##INFO=<ID=" + CGD_INHERITANCE.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=String,Description=\"CGD_INHERITANCE\">",
			"##INFO=<ID=" + CGD_GENERALIZED_INHERITANCE.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=String,Description=\"CGD_GENERALIZED_INHERITANCE\">", });

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

	public ClinicalGenomicsDatabaseServiceAnnotator(File cgdFileLocation, File inputVcfFile, File outputVCFFile)
			throws Exception
	{

		runningFromCommandLine = true;

		// TODO: replace with snpeff
		hgncLocationsProvider = null;

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(CGD_FILE_LOCATION, cgdFileLocation.getAbsolutePath());
		cgdDataProvider = new CgdDataProvider(molgenisSettings);

		this.annotatorService = new AnnotationServiceImpl();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				CGD_CONDITION.substring(VcfRepository.getInfoPrefix().length()));

		System.out.println("Now starting to process the data.");

		while (vcfIter.hasNext())
		{
			Entity record = vcfIter.next();

			List<Entity> annotatedRecord = annotateEntity(record);

			if (annotatedRecord.size() > 1)
			{
				outputVCFWriter.close();
				vcfRepo.close();
				throw new Exception("Multiple outputs for " + record.toString());
			}
			else if (annotatedRecord.size() == 0)
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(record));
			}
			else
			{
				outputVCFWriter.println(VcfUtils.convertToVCF(annotatedRecord.get(0)));
			}
		}
		outputVCFWriter.close();
		vcfRepo.close();
		System.out.println("All done!");

	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public String getSimpleName()
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

		Long position = entity.getLong(VcfRepository.POS);
		String chromosome = entity.getString(VcfRepository.CHROM);

        String geneSymbol = HgncLocationsUtils.locationToHgcn(hgncLocationsProvider.getHgncLocations(),
                new Locus(chromosome, position)).get(0);

        Map<String, CgdData> cgdData = cgdDataProvider.getCgdData();

		try
		{
			HashMap<String, Object> resultMap = new HashMap<String, Object>();

			if (cgdData.containsKey(geneSymbol))
			{
				CgdData data = cgdData.get(geneSymbol);

				resultMap.put(CGD_CONDITION, data.getCondition().replace(";", " /").replace(",", ""));
				resultMap.put(CGD_INHERITANCE, data.getInheritance().replace(";", " /").replace(",", ""));
				resultMap.put(CGD_GENERALIZED_INHERITANCE, data.getGeneralizedInheritance());
				resultMap.put(CGD_AGE_GROUP, data.getAge_group().replace(";", " /").replace(",", ""));

				if (!runningFromCommandLine)
				{
					resultMap.put(GENE, geneSymbol);
					resultMap.put(HGNC_ID, data.getHgnc_id());
					resultMap.put(ENTREZ_GENE_ID, data.getEntrez_gene_id());
					resultMap.put(ALLELIC_CONDITIONS, data.getAllelic_conditions());
					resultMap.put(MANIFESTATION_CATEGORIES, data.getManifestation_categories());
					resultMap.put(INTERVENTION_CATEGORIES, data.getIntervention_categories());
					resultMap.put(COMMENTS, data.getComments());
					resultMap.put(INTERVENTION_RATIONALE, data.getIntervention_rationale());
					resultMap.put(REFERENCES, data.getReferences());
					resultMap.put(VcfRepository.CHROM, chromosome);
					resultMap.put(VcfRepository.POS, position);
				}

				results.add(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
			}
			else
			{
				results.add(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
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
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ENTREZ_GENE_ID,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CGD_CONDITION, MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CGD_INHERITANCE,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CGD_GENERALIZED_INHERITANCE,
				MolgenisFieldTypes.FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CGD_AGE_GROUP, MolgenisFieldTypes.FieldTypeEnum.TEXT));
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
