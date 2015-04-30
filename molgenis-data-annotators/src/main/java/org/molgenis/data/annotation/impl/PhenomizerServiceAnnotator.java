package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.utils.VcfUtils;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Adds Phenomizer P-values
 * 
 * */
@Component("phenomizerService")
public class PhenomizerServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(PhenomizerServiceAnnotator.class);

	private final AnnotationService annotatorService;

    public static final String PHENOMIZERPVAL_LABEL = "PHENOMIZERPVAL";
    public static final String PHENOMIZEROMIM_LABEL = "PHENOMIZEROMIM";
    public static final String PHENOMIZERPVAL = VcfRepository.getInfoPrefix() + PHENOMIZERPVAL_LABEL;
    public static final String PHENOMIZEROMIM = VcfRepository.getInfoPrefix() + PHENOMIZEROMIM_LABEL;


    private static final String NAME = "PHENOMIZER";

	HashMap<String, String> geneToPval;
	HashMap<String, String> geneToOmimID;

	final List<String> infoFields = Arrays.asList(new String[]
	{
			"##INFO=<ID=" + PHENOMIZERPVAL.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=Float,Description=\"Phenomizer P-value\">",
			"##INFO=<ID=" + PHENOMIZEROMIM.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=String,Description=\"Phenomizer OMIM ID\">", });

	@Autowired
	public PhenomizerServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.annotatorService = annotatorService;
	}

	public static List<String> getHtml(String url) throws IOException
	{
		List<String> lines = new ArrayList<>();
		URL loc = new URL(url);
		BufferedReader in = new BufferedReader(new InputStreamReader(loc.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null)
		{
			lines.add(inputLine);
		}
		in.close();
		return lines;
	}

	public PhenomizerServiceAnnotator(File hpoTermFile, File inputVcfFile, File outputVCFFile) throws Exception
	{

		int limit = 10000;

		/**
		 * Check input HPO terms
		 */
		Scanner s = new Scanner(hpoTermFile);
		String hpoTerms = s.nextLine();
		if (s.hasNextLine())
		{
			throw new IOException(
					"HPO terms file is not supposed to have more than 1 line. Example line: HP:0001300,HP:0007325,HP:0002015");
		}

		hpoTerms = hpoTerms.trim();

		LOG.info("Line in HPO terms file (trimmed): " + hpoTerms);
		String[] splitTerms = hpoTerms.split(",", -1);
		for (String term : splitTerms)
		{
			if (!term.startsWith("HP:"))
			{
				throw new IOException(
						"HPO term did not start with 'HP:'. Example line: HP:0001300,HP:0007325,HP:0002015");
			}
			else
			{
				LOG.info("Term OK: " + term);
			}
		}

		/**
		 * Invoke web service
		 */
		List<String> response = getHtml("http://compbio.charite.de/phenomizer/phenomizer/PhenomizerServiceURI?mobilequery=true&numres="
				+ limit + "&terms=" + hpoTerms);

		String pval = null;
		String omimId = null;
		String gene = null;

		geneToPval = new HashMap<>();
		geneToOmimID = new HashMap<>();

		for (String line : response)
		{
			if (line == null || line.equals("") || line.startsWith("#"))
			{
				continue;
			}
			String[] split = line.split("\t", -1);
			pval = split[0];
			omimId = split[2];
			gene = split[4];

			for (String multiGene : gene.split(", ", -1))
			{
				geneToPval.put(multiGene, pval);
				geneToOmimID.put(multiGene, omimId);
			}

		}

		for (String key : geneToPval.keySet())
		{
			LOG.info("gene: " + key + ", pval: " + geneToPval.get(key));
			LOG.info("gene: " + key + ", " + geneToOmimID.get(key));
		}

		this.annotatorService = new AnnotationServiceImpl();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				PHENOMIZERPVAL.substring(VcfRepository.getInfoPrefix().length()));

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
		Map<String, Object> resultMap = annotateEntityWithPhenomizerPvalue(entity);
		return Collections.singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	private synchronized Map<String, Object> annotateEntityWithPhenomizerPvalue(Entity entity) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String[] annSplit = entity.getString(VcfRepository.getInfoPrefix() + "ANN").split("\\|", -1);
		String gene = null;

		if (annSplit[3].length() != 0)
		{
			gene = annSplit[3];
			resultMap.put(PHENOMIZERPVAL, geneToPval.get(gene));
			resultMap.put(PHENOMIZEROMIM, geneToOmimID.get(gene));
		}
		else
		{
			// do nothing, will happen a lot for WGS data
		}

		return resultMap;

	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PHENOMIZERPVAL, FieldTypeEnum.DECIMAL).setLabel(PHENOMIZERPVAL_LABEL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PHENOMIZEROMIM, FieldTypeEnum.STRING).setLabel(PHENOMIZEROMIM_LABEL));
		return metadata;
	}

}
