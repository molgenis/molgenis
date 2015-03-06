package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.LocusAnnotator;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.annotation.impl.datastructures.HpoData;
import org.molgenis.data.annotation.provider.HpoDataProvider;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Typical HPO terms for a gene identifier (already present via SnpEff)
 * Source: http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastStableBuild/artifact/annotation/ALL_SOURCES_TYPICAL_FEATURES_diseases_to_genes_to_phenotypes.txt
 * 
 *
 */

@Component("HpoService")
public class HpoServiceAnnotator extends LocusAnnotator
{
	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;
	private final HpoDataProvider hpoDataProvider;

	private static final String NAME = "HPO";

	public static final String HPO_IDS = VcfRepository.getInfoPrefix() + "HPOIDS";
	public static final String HPO_TERMS = VcfRepository.getInfoPrefix() + "HPOTERMS";
	public static final String HPO_FILE_LOCATION = "hpo_location";
	
	
	final List<String> infoFields = Arrays
			.asList(new String[]
			{
				"##INFO=<ID=" + HPO_IDS.substring(VcfRepository.getInfoPrefix().length()) + ",Number=1,Type=String,Description=\"HPO identifiers\">",
				"##INFO=<ID=" + HPO_TERMS.substring(VcfRepository.getInfoPrefix().length()) + ",Number=1,Type=String,Description=\"HPO terms\">",
			});


	@Autowired
	public HpoServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotationService, HpoDataProvider hpoDataProvider) throws IOException
	{
		if (molgenisSettings == null) throw new IllegalArgumentException("molgenisSettings is null");
		if (annotationService == null) throw new IllegalArgumentException("annotationService is null");
		if (hpoDataProvider == null) throw new IllegalArgumentException("hpoDataProvider is null");
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotationService;
		this.hpoDataProvider = hpoDataProvider;
	}
	
	public HpoServiceAnnotator(File hpoFileLocation, File inputVcfFile, File outputVCFFile) throws Exception
	{	
		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(HPO_FILE_LOCATION, hpoFileLocation.getAbsolutePath());
		hpoDataProvider = new HpoDataProvider(molgenisSettings);

		this.annotatorService = new AnnotationServiceImpl();
		
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields, HPO_IDS.substring(VcfRepository.getInfoPrefix().length()));

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
		if (null == molgenisSettings.getProperty(HPO_FILE_LOCATION)) return false;
		return new File(molgenisSettings.getProperty(HPO_FILE_LOCATION)).exists();
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException
	{
		
		List<Entity> results = new ArrayList<Entity>();
		String geneSymbol = null;

		String annField = entity.getString(VcfRepository.getInfoPrefix()+"ANN");
		if(annField != null)
		{
			String[] split = annField.split("\\|", -1);
			if(split.length > 10)
			{
				//3 is 'gene name', 4 is 'gene id' .. which ??
				if(split[3].length() != 0)
				{
					geneSymbol = split[3];
					//too much..
					//LOG.info("Gene symbol '" + geneSymbol + "' found for " + entity.toString());
				}
				else
				{
					//will happen a lot for WGS data
					//LOG.info("No gene symbol in ANN field for " + entity.toString());
					return Collections.singletonList(entity);
				}
				
			}
		}
		
		Map<String, List<HpoData>> hpoData = hpoDataProvider.getHpoData();

		try
		{
			HashMap<String, Object> resultMap = new HashMap<String, Object>();

			if (hpoData.containsKey(geneSymbol))
			{
				List<HpoData> data = hpoData.get(geneSymbol);
				
				StringBuilder ids = new StringBuilder();
				StringBuilder terms = new StringBuilder();
				for(HpoData h : data)
				{
					ids.append(h.getHpoId());
					ids.append("/");
					terms.append(h.getHpoTerm());
					terms.append("/");
				}
				if(ids.length() > 0){
					ids.deleteCharAt(ids.length()-1);
					resultMap.put(HPO_IDS, ids);
				}
				if(terms.length() > 0){
					terms.deleteCharAt(terms.length()-1);
					resultMap.put(HPO_TERMS, terms);
				}
				results.add(getAnnotatedEntity(entity, resultMap));
			}
			else
			{
				return Collections.singletonList(entity);
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
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_IDS, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HPO_TERMS, MolgenisFieldTypes.FieldTypeEnum.STRING));
		return metadata;
	}
}
