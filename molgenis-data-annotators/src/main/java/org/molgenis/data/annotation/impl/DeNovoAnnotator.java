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
import org.molgenis.data.annotation.TabixReader;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.VcfUtils;
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
 * 
 * De novo variant annotator
 * Uses trio data (child-mother-father), read from VCF pedigree data
 * See: http://samtools.github.io/hts-specs/VCFv4.2.pdf
 * 
 *
 **/
@Component("deNovoService")
public class DeNovoAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(DeNovoAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;
	private HashMap<String, List<String>> pedigree;


	public static final String DENOVO = VcfRepository.getInfoPrefix() + "DENOVO";

	private static final String NAME = "DENOVO";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ DENOVO.substring(VcfRepository.getInfoPrefix().length())
							+ ",Number=1,Type=String,Description=\"todo\">"
							});

	@Autowired
	public DeNovoAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public DeNovoAnnotator(File exacFileLocation, File inputVcfFile, File outputVCFFile) throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		this.annotatorService = new AnnotationServiceImpl();
		
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields, DENOVO.substring(VcfRepository.getInfoPrefix().length()));

		pedigree = VcfUtils.getPedigree(inputVcfFile);
		for(String key : pedigree.keySet())
		{
			System.out.println("CHILD: " + key + ", M/F: " + pedigree.get(key));
		}
		
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
		return true; //no annotation data required ?
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		Map<String, Object> resultMap = annotateEntityWithDeNovo(entity);
		return Collections.<Entity> singletonList(getAnnotatedEntity(entity, resultMap));
	}

	

	private synchronized Map<String, Object> annotateEntityWithDeNovo(Entity entity) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		Iterable<Entity> samples = entity.getEntities("Samples");
		
		System.out.println("Variant: " + entity.get(VcfRepository.CHROM) + " " + entity.get(VcfRepository.POS) + " " + entity.get(VcfRepository.REF) + " " + entity.get(VcfRepository.ALT));
		for(Entity sample : samples)
		{
			String sampleID = sample.get("NAME").toString().substring(sample.get("NAME").toString().lastIndexOf("_")+1);
			System.out.println("sampleID " + sampleID);
			System.out.println("\t" + sample.get("GT") + " is a " + (pedigree.containsKey(sampleID) ? "child" : "parent"));
		}
		
		resultMap.put(DENOVO, "yes");
		return resultMap;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(DENOVO, FieldTypeEnum.STRING));
		return metadata;
	}

}
