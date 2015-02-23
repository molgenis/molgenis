package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.annotation.provider.CgdDataProvider;
import org.molgenis.data.annotation.provider.CgdDataProvider.generalizedInheritance;
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

/**
 * Mendelian disease filter
 * 
 * */
@Component("mendelService")
public class MendelianDiseaseCandidatesServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(MendelianDiseaseCandidatesServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	public static final String MENDELDISEASECANDIDATE = VcfRepository.getInfoPrefix() + "MENDDISCAND";

	private static final String NAME = "MENDELIANDISEASE";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ MENDELDISEASECANDIDATE.substring(VcfRepository.getInfoPrefix().length())
							+ ",Number=1,Type=Integer,Description=\"Boolean? 0 = ruled out, 1 = candidate? TODO: Possibly 2 for strong candidate? or top 10 or so?\">",
								});
	
	@Autowired
	public MendelianDiseaseCandidatesServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public MendelianDiseaseCandidatesServiceAnnotator(File filterSettings, File inputVcfFile, File outputVCFFile) throws Exception
	{

		//TODO: filterSettings ??
		
		this.molgenisSettings = new MolgenisSimpleSettings();
		this.annotatorService = new AnnotationServiceImpl();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkInput(inputVcfFile, outputVCFWriter, infoFields, MENDELDISEASECANDIDATE.substring(VcfRepository.getInfoPrefix().length()));

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
	public boolean annotationDataExists()
	{
		return false;
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		Map<String, Object> resultMap = annotateEntityWithMendelianDiseaseCandidates(entity);
		return Collections.<Entity> singletonList(getAnnotatedEntity(entity, resultMap));
	}

	private synchronized Map<String, Object> annotateEntityWithMendelianDiseaseCandidates(Entity entity) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		double thGenMAF = entity.getDouble(VcfRepository.getInfoPrefix() + ThousandGenomesServiceAnnotator.THGEN_MAF);
		double exacMAF = entity.getDouble(VcfRepository.getInfoPrefix() + ExACServiceAnnotator.EXAC_MAF);
		double gonlMAF = entity.getDouble(VcfRepository.getInfoPrefix() + GoNLServiceAnnotator.GONL_MAF);
		CgdDataProvider.generalizedInheritance cgdGenInh = generalizedInheritance.valueOf(entity.getString(ClinicalGenomicsDatabaseServiceAnnotator.CGD_GENERALIZED_INHERITANCE));
		SnpEffServiceAnnotator.impact impact = SnpEffServiceAnnotator.impact.valueOf(entity.getString("ANN").split("|")[2]);
		
		//now filter.. and set boolean :-)
		
		
		
		//FIXME: actually, they should also be inside INFO compound attribute!!
		resultMap.put(MENDELDISEASECANDIDATE, 0);
		return resultMap;

	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(MENDELDISEASECANDIDATE, FieldTypeEnum.INT)); //FIXME best type?
		return metadata;
	}

}
