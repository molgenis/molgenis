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
import org.molgenis.data.annotation.cmd.AnnotatorInfo;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotation.utils.TabixReader;
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
 * ExAC annotator
 * 
 * Data @ ftp://ftp.broadinstitute.org/pub/ExAC_release/release0.3/
 * 
 * TODO: feature enhancement: match multiple alternatives from SOURCE file to multiple alternatives in ExAC
 * 
 * e.g. 1 237965145 rs115779425 T TT,TTT,TTTT . PASS AC=31,3,1;AN=70;GTC=0,31,0,3,0,0,1,0,0,0;
 * 
 * 
 * */
@Component("exacService")
public class ExACServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(ExACServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	@Override
	public AnnotatorInfo.status getStatus(){
		return AnnotatorInfo.status.BETA;
	}

	@Override
	public AnnotatorInfo.type getType(){
		return AnnotatorInfo.type.POPULATION_REFERENCE;
	}
	
	@Override
	public String getCode()
	{
		return "exac";
	}
	
	public static final String EXAC_MAF_LABEL = "EXACMAF";
	public static final String EXAC_MAF = VcfRepository.getInfoPrefix() + EXAC_MAF_LABEL;

	private static final String NAME = "EXAC";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{ "##INFO=<ID="
					+ EXAC_MAF.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=Float,Description=\"ExAC minor allele frequency. Taken straight for AF field, inverted when alleles are swapped\">" });

	public static final String EXAC_VCFGZ_LOCATION = "exac_location";

	private volatile TabixReader tabixReader;

	@Autowired
	public ExACServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public ExACServiceAnnotator(File exacFileLocation, File inputVcfFile, File outputVCFFile) throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(EXAC_VCFGZ_LOCATION, exacFileLocation.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		checkTabixReader();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				EXAC_MAF.substring(VcfRepository.getInfoPrefix().length()));

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
		boolean canAnnotate = false;
		if (molgenisSettings.getProperty(EXAC_VCFGZ_LOCATION) != null)
		{
			canAnnotate = new File(molgenisSettings.getProperty(EXAC_VCFGZ_LOCATION)).exists();
		}
		return canAnnotate;
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		checkTabixReader();

		Map<String, Object> resultMap = annotateEntityWithExAC(entity.getString(VcfRepository.CHROM),
				entity.getLong(VcfRepository.POS), entity.getString(VcfRepository.REF),
				entity.getString(VcfRepository.ALT));
		return Collections.<Entity> singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	/**
	 * Makes sure the tabixReader exists.
	 */
	private void checkTabixReader() throws IOException
	{
		if (tabixReader == null)
		{
			synchronized (this)
			{
				if (tabixReader == null)
				{
					tabixReader = new TabixReader(molgenisSettings.getProperty(EXAC_VCFGZ_LOCATION));
				}
			}
		}
	}

	private synchronized Map<String, Object> annotateEntityWithExAC(String chromosome, Long position, String reference,
			String alternative) throws IOException
	{
		TabixReader.Iterator tabixIterator = null;
		try
		{
			tabixIterator = tabixReader.query(chromosome + ":" + position + "-" + position);
		}
		catch (Exception e)
		{
			LOG.error("Something went wrong (chromosome not in data?) when querying ExAC tabix file for " + chromosome
					+ " POS: " + position + " REF: " + reference + " ALT: " + alternative + "! skipping...");
		}
		String line = null;

		// get line from data, we expect exactly 1
		try
		{
			line = tabixIterator.next();
		}
		catch (net.sf.samtools.SAMFormatException sfx)
		{
			LOG.error("Bad GZIP file for CHROM: " + chromosome + " POS: " + position + " REF: " + reference + " ALT: "
					+ alternative + " LINE: " + line);
			throw sfx;
		}
		catch (NullPointerException npe)
		{
			// overkill to print all missing, since ExAC is exome data 'only'
		}

		// if nothing found, return empty list for no hit
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (line == null)
		{
			return resultMap;
		}

		// sanity check on content of line
		String[] split = null;
		split = line.split("\t", -1);
		if (split.length != 8)
		{
			LOG.error("Bad ExAC data (split was not 8 elements) for CHROM: " + chromosome + " POS: " + position
					+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
			throw new IOException("Bad data! see log");
		}

		// get MAF from info field
		String[] infoFields = split[7].split(";", -1);
		String[] mafs = null;
		for (String info : infoFields)
		{
			if (info.startsWith("AF="))
			{
				try
				{
					mafs = info.replace("AF=", "").split(",", -1);
					break;
				}
				catch (java.lang.NumberFormatException e)
				{
					LOG.error("Could not get MAF for line \n" + line);
				}
			}
		}

		// get alt alleles and check if the amount is equal to MAF list
		String[] altAlleles = split[4].split(",", -1);
		if (mafs.length != altAlleles.length)
		{
			throw new IOException("Number of alt alleles unequal to number of MAF values for line " + line);
		}

		// match alleles and get the MAF from list
		Double maf = null;
		for (int i = 0; i < altAlleles.length; i++)
		{
			String altAllele = altAlleles[i];
			if (altAllele.equals(alternative) && split[3].equals(reference))
			{
				maf = Double.parseDouble(mafs[i]);
			}
		}

		// if nothing found, try swapping ref-alt, and do 1-MAF
		if (false && maf == null) // FIXME TODO See 1000G annotator why this is not (always) allowed
		{
			for (int i = 0; i < altAlleles.length; i++)
			{
				String altAllele = altAlleles[i];
				if (altAllele.equals(reference) && split[3].equals(alternative))
				{
					maf = 1 - Double.parseDouble(mafs[i]);
					LOG.info("*ref-alt swapped* ExAC variant found for CHROM: " + chromosome + " POS: " + position
							+ " REF: " + reference + " ALT: " + alternative + ", MAF (1-originalMAF) = " + maf);
				}
			}
		}

		resultMap.put(EXAC_MAF, maf);
		return resultMap;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(EXAC_MAF, FieldTypeEnum.DECIMAL)
				.setLabel(EXAC_MAF_LABEL));

		return metadata;
	}

}
