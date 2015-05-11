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
 * GoNl annotator data:
 * 
 * https://molgenis26.target.rug.nl/downloads/gonl_public/variants/release5_with_GTC/
 * release5_noContam_noChildren_with_AN_AC_GTC_stripped.tgz
 * 
 * PLUS chrX from http://molgenis15.target.rug.nl/release4_noContam_noChildren_with_AN_AC_GTC_stripped.tgz
 * 
 * 
 * 
 * GoNL example line: 1 126108 rs146756510 G A . PASS AC=37;AN=996;DB;GTC=461,37,0;set=SNP
 * 
 * 
 * TODO: right now there are no multiple alternative alleles in GoNL, but will there be in the future??
 * 
 * 
 * */
@Component("gonlService")
public class GoNLServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(GoNLServiceAnnotator.class);

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
		return "gonl";
	}

	public static final String GONL_MAF_LABEL = "GONLMAF";
	public static final String GONL_GTC_LABEL = "GONLGTC";

	public static final String GONL_MAF = VcfRepository.getInfoPrefix() + GONL_MAF_LABEL;
	public static final String GONL_GTC = VcfRepository.getInfoPrefix() + GONL_GTC_LABEL;

	private static final String NAME = "GONL";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ GONL_MAF.substring(VcfRepository.getInfoPrefix().length())
							+ ",Number=1,Type=Float,Description=\"GoNL minor allele frequency. Calculated by dividing AC by AN. For example: AC=23;AN=996 = 0.02309237\">",
					"##INFO=<ID="
							+ GONL_GTC.substring(VcfRepository.getInfoPrefix().length())
							+ ",Number=G,Type=Integer,Description=\"GoNL genotype counts. For example: GONLGTC=69,235,194. Listed in the same order as the ALT alleles in case multiple ALT alleles are present = 0/0,0/1,1/1,0/2,1/2,2/2,0/3,1/3,2/3,3/3,etc. Phasing is ignored; hence 0/1, 1/0, 0|1 and 1|0 are all counted as 0/1. Incomplete gentotypes (./., ./0, ./1, ./2, etc.) are completely discarded for calculating GTC.\">" });

	public static final String GONL_DIRECTORY_LOCATION_PROPERTY = "gonl_location";

	HashMap<String, TabixReader> tabixReaders = null;

	@Autowired
	public GoNLServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public GoNLServiceAnnotator(File gonlR5directory, File inputVcfFile, File outputVCFFile) throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(GONL_DIRECTORY_LOCATION_PROPERTY, gonlR5directory.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		getTabixReaders();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				GONL_MAF.substring(VcfRepository.getInfoPrefix().length()));

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
		if (null == molgenisSettings.getProperty(GONL_DIRECTORY_LOCATION_PROPERTY)) return false;
		File f = new File(molgenisSettings.getProperty(GONL_DIRECTORY_LOCATION_PROPERTY));
		return (f.exists() && f.isDirectory());
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		getTabixReaders();
		Map<String, Object> resultMap = annotateWithGoNL(entity.getString(VcfRepository.CHROM),
				entity.getLong(VcfRepository.POS), entity.getString(VcfRepository.REF),
				entity.getString(VcfRepository.ALT));
		return Collections.<Entity> singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	/**
	 * Makes sure the tabixReader exists.
	 */
	private void getTabixReaders() throws IOException
	{
		if (tabixReaders == null)
		{
			synchronized (this)
			{
				if (tabixReaders == null)
				{
					tabixReaders = new HashMap<String, TabixReader>();
					String chroms = "1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|X";

					for (String chr : chroms.split("\\|"))
					{
						String gonlchrom = new String(
								molgenisSettings.getProperty(GONL_DIRECTORY_LOCATION_PROPERTY)
										+ File.separator
										// small hack to use chrX from release 4.. (5 isn't out yet!)
										+ (!chr.equals("X") ? "gonl.chr" + chr + ".snps_indels.r5.vcf.gz" : "gonl.chrX.release4.gtc.vcf.gz"));
						if (new File(gonlchrom).exists())
						{
							TabixReader tr = new TabixReader(gonlchrom);
							tabixReaders.put(chr, tr);
						}
						else
						{
							LOG.info("No file found for path: " + gonlchrom);
						}
					}
				}
			}
		}
	}

	private synchronized Map<String, Object> annotateWithGoNL(String inputChromosome, Long inputPosition,
			String inputReference, String inputAlternative) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (!tabixReaders.containsKey(inputChromosome))
		{
			LOG.info("No chromosome " + inputChromosome + " in data!");
			return resultMap;
		}

		TabixReader.Iterator tabixIterator = tabixReaders.get(inputChromosome).query(
				inputChromosome + ":" + inputPosition + "-" + inputPosition);
		String line = null;

		// get line from data, we expect exactly 1
		try
		{
			line = tabixIterator.next();
		}
		catch (net.sf.samtools.SAMFormatException sfx)
		{
			LOG.error("Bad GZIP file for CHROM: " + inputChromosome + " POS: " + inputPosition + " REF: "
					+ inputReference + " ALT: " + inputAlternative + " LINE: " + line);
			throw sfx;
		}
		catch (NullPointerException npe)
		{
			LOG.info("No data for CHROM: " + inputChromosome + " POS: " + inputPosition + " REF: " + inputReference
					+ " ALT: " + inputAlternative + " LINE: " + line);
			// throw sfx;
		}

		// if nothing found, return empty list for no hit
		if (line == null)
		{
			return resultMap;
		}

		// sanity check on content of line
		String[] split = null;
		split = line.split("\t", -1);
		if (split.length != 8)
		{
			LOG.error("Bad GoNL data (split was not 8 elements) for CHROM: " + inputChromosome + " POS: "
					+ inputPosition + " REF: " + inputReference + " ALT: " + inputAlternative + " LINE: " + line);
			throw new IOException("Bad data! see log");
		}

		// get MAF from info field
		String[] infoFields = split[7].split(";", -1);
		double ac = -1;
		double an = -1;
		String gtc = null;
		for (String info : infoFields)
		{
			if (info.startsWith("AC="))
			{
				ac = Double.parseDouble(info.replace("AC=", ""));
			}
			if (info.startsWith("AN="))
			{
				an = Double.parseDouble(info.replace("AN=", ""));
			}
			if (info.startsWith("GTC="))
			{
				gtc = info.replace("GTC=", "");
			}
		}

		// check if we miss data
		if (ac == -1 || an == -1)
		{
			LOG.error("Bad GoNL data (no AC or AN field) for CHROM: " + inputChromosome + " POS: " + inputPosition
					+ " REF: " + inputReference + " ALT: " + inputAlternative + " LINE: " + line);
			throw new IOException("Bad data (no AC or AN), see log");
		}
		if (gtc == null)
		{
			LOG.error("Bad GoNL data (no GTC field) for CHROM: " + inputChromosome + " POS: " + inputPosition
					+ " REF: " + inputReference + " ALT: " + inputAlternative + " LINE: " + line);
			throw new IOException("Bad data (no GTC), see log");
		}

		String ref = split[3];
		String alt = split[4];

		Double maf = null;

		// match alleles and get the Minor Allele Frequency from list
		if (ref.equals(inputReference) && alt.equals(inputAlternative))
		{
			maf = ac / an;
		}

		// if nothing found, try swapping ref-alt, and do 1-MAF
		if (false && maf == null) // FIXME TODO See 1000G annotator why this is not (always) allowed
		{

			if (ref.equals(inputAlternative) && alt.equals(inputReference))
			{
				maf = 1 - (ac / an);
				LOG.info("*ref-alt swapped* 1000G variant found for CHROM: " + inputChromosome + " POS: "
						+ inputPosition + " REF: " + inputReference + " ALT: " + inputAlternative
						+ ", MAF (1-originalMAF) = " + maf);
			}

		}

		resultMap.put(GONL_GTC, gtc);
		resultMap.put(GONL_MAF, maf);
		return resultMap;

	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GONL_MAF, FieldTypeEnum.DECIMAL)
				.setLabel(GONL_MAF_LABEL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GONL_GTC, FieldTypeEnum.STRING)
				.setLabel(GONL_GTC_LABEL));

		return metadata;
	}

}
