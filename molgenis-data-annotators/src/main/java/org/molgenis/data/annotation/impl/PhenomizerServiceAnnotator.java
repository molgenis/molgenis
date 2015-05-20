package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.DataConverter;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.provider.UrlPinger;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.vcf.utils.VcfUtils;
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
 * Adds Phenomizer P-values
 * 
 * */
@Component("phenomizerService")
public class PhenomizerServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(PhenomizerServiceAnnotator.class);
	public static final String KEY_PHENOMIZER_URL = ""; // "http://compbio.charite.de/phenomizer/phenomizer/PhenomizerServiceURI";
	private final AnnotationService annotatorService;

	public static final String PHENOMIZERPVAL_LABEL = "PHENOMIZERPVAL";
	public static final String PHENOMIZEROMIM_LABEL = "PHENOMIZEROMIM";
	public static final String PHENOMIZERPVAL = VcfRepository.getInfoPrefix() + PHENOMIZERPVAL_LABEL;
	public static final String PHENOMIZEROMIM = VcfRepository.getInfoPrefix() + PHENOMIZEROMIM_LABEL;
	private final MolgenisSettings molgenisSettings;
	private final UrlPinger urlPinger;

	private static final String NAME = "PHENOMIZER";

	HashMap<String, String> geneToPval;
	HashMap<String, String> geneToOmimID;
	int limit = 10000;

	final List<String> infoFields = Arrays.asList(new String[]
	{
			"##INFO=<ID=" + PHENOMIZERPVAL.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=Float,Description=\"Phenomizer P-value\">",
			"##INFO=<ID=" + PHENOMIZEROMIM.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=String,Description=\"Phenomizer OMIM ID\">", });

	@Autowired
	public PhenomizerServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService,
			UrlPinger urlPinger) throws IOException
	{
		this.annotatorService = annotatorService;
		this.molgenisSettings = molgenisSettings;
		this.urlPinger = urlPinger;
	}

	public static List<String> getHtml(BufferedReader in) throws IOException
	{
		List<String> lines = new ArrayList<>();
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
		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(KEY_PHENOMIZER_URL,
				"http://compbio.charite.de/phenomizer/phenomizer/PhenomizerServiceURI");
		this.urlPinger = new UrlPinger();

		/**
		 * Check input HPO terms
		 */
		Scanner s = new Scanner(hpoTermFile, "UTF-8");
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

		this.annotatorService = new AnnotationServiceImpl();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				PHENOMIZERPVAL.substring(VcfRepository.getInfoPrefix().length()));

		URL loc = new URL(molgenisSettings.getProperty(KEY_PHENOMIZER_URL, "") + "?mobilequery=true&numres=" + limit
				+ "&terms=" + hpoTerms);
		BufferedReader in = new BufferedReader(new InputStreamReader(loc.openStream(), StandardCharsets.UTF_8));

		System.out.println("Now starting to process the data.");

		while (vcfIter.hasNext())
		{
			Entity record = vcfIter.next();

			List<Entity> annotatedRecord = annotateEntityWithPhenomizerPvalue(record, in);

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

	public void invokeWebService(BufferedReader in) throws IOException
	{

		List<String> response = getHtml(in);

		String pval;
		String omimId;
		String gene;

		geneToPval = new HashMap<>();
		geneToOmimID = new HashMap<>();

		for (String line : response)
		{
			if (line == null || line.equals("") || line.startsWith("#"))
			{
				continue;
			}
			String[] split = line.split("\t", -1);
			if (split.length > 4)
			{
				pval = split[0];
				omimId = split[2];
				gene = split[4];
				for (String multiGene : gene.split(", ", -1))
				{
					geneToPval.put(multiGene, pval);
					geneToOmimID.put(multiGene, omimId);
				}
			}
			else
			{
				LOG.info("Output does not contain enough information to use for annotation");
			}
		}
		for (String key : geneToPval.keySet())
		{
			LOG.info("gene: " + key + ", pval: " + geneToPval.get(key));
			LOG.info("gene: " + key + ", " + geneToOmimID.get(key));
		}
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public boolean annotationDataExists()
	{
		boolean dataExists = false;
		if (urlPinger.ping(molgenisSettings.getProperty(KEY_PHENOMIZER_URL, ""), 500))
		{
			dataExists = true;
		}
		return dataExists;
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		String hpoTerms = entity.getString(HpoServiceAnnotator.HPO_TERMS);
		URL loc = new URL(molgenisSettings.getProperty(KEY_PHENOMIZER_URL, "") + "?mobilequery=true&numres=" + limit
				+ "&terms=" + hpoTerms);
		BufferedReader in = new BufferedReader(new InputStreamReader(loc.openStream(), Charset.forName("UTF8")));

		return annotateEntityWithPhenomizerPvalue(entity, in);
	}

	protected synchronized List<Entity> annotateEntityWithPhenomizerPvalue(Entity entity, BufferedReader in)
			throws IOException
	{
		Map<String, Object> resultMap = new HashMap<>();
		String[] annSplit = entity.getString(VcfRepository.getInfoPrefix() + "ANN").split("\\|", -1);
		String gene;

		invokeWebService(in);

		if (annSplit[3].length() != 0)// else do nothing, will happen a lot for WGS data
		{
			gene = annSplit[3];
			resultMap.put(PHENOMIZERPVAL, DataConverter.toDouble(geneToPval.get(gene)));
			resultMap.put(PHENOMIZEROMIM, geneToOmimID.get(gene));
		}
		return Collections.singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PHENOMIZERPVAL, FieldTypeEnum.DECIMAL)
				.setLabel(PHENOMIZERPVAL_LABEL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(PHENOMIZEROMIM, FieldTypeEnum.STRING)
				.setLabel(PHENOMIZEROMIM_LABEL));
		return metadata;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(VcfRepository.getInfoPrefix() + "ANN",
				FieldTypeEnum.TEXT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(HpoServiceAnnotator.HPO_TERMS,
				MolgenisFieldTypes.FieldTypeEnum.STRING));
		return metadata;
	}

}
