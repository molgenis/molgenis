package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotator.tabix.TabixReader;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * 1000G annotator
 * 
 * Data @ http://ftp.1000genomes.ebi.ac.uk/vol1/ftp/release/20130502/
 * 
 * TODO: feature enhancement: match multiple alternatives from SOURCE file to multiple alternatives in 1000G
 * 
 * e.g. 1 237965145 rs115779425 T TT,TTT,TTTT . PASS AC=31,3,1;AN=70;GTC=0,31,0,3,0,0,1,0,0,0;
 * 
 * 
 * 1000G example line: 1 10352 rs145072688 T TA 100 PASS
 * AC=2191;AF=0.4375;AN=5008;NS=2504;DP=88915;EAS_AF=0.4306;AMR_AF=
 * 0.4107;AFR_AF=0.4788;EUR_AF=0.4264;SAS_AF=0.4192;AA=|||unknown(NO_COVERAGE) G 1|0 1|0 0|1 0|1 1|0 1|0 [+many more
 * genotypes]
 *
 **/
@Component("thousandGenomesService")
public class ThousandGenomesServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(ThousandGenomesServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private static final String NAME = "1000G";
	public static final String THGEN_MAF_LABEL = "1KGMAF";
	public static final String THGEN_MAF = VcfRepository.getInfoPrefix() + THGEN_MAF_LABEL;
	public static final String THGEN_DIRECTORY_LOCATION_PROPERTY = "1000G_location";

	final List<String> infoFields = Arrays.asList(new String[]
	{ "##INFO=<ID=" + THGEN_MAF.substring(VcfRepository.getInfoPrefix().length())
			+ ",Number=1,Type=Float,Description=\"1000G minor allele frequency.\">" });

	HashMap<String, TabixReader> tabixReaders = null;

	@Autowired
	public ThousandGenomesServiceAnnotator(MolgenisSettings molgenisSettings) throws IOException
	{
		this.molgenisSettings = molgenisSettings;
	}

	public ThousandGenomesServiceAnnotator(File thGenDir, File inputVcfFile, File outputVCFFile) throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(THGEN_DIRECTORY_LOCATION_PROPERTY, thGenDir.getAbsolutePath());

		getTabixReaders();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, getOutputMetaData(),
				THGEN_MAF.substring(VcfRepository.getInfoPrefix().length()));

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
	public boolean annotationDataExists()
	{
		if (null == molgenisSettings.getProperty(THGEN_DIRECTORY_LOCATION_PROPERTY)) return false;
		File f = new File(molgenisSettings.getProperty(THGEN_DIRECTORY_LOCATION_PROPERTY));
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
		Map<String, Object> resultMap = annotateWith1000G(entity.getString(VcfRepository.CHROM),
				entity.getLong(VcfRepository.POS), entity.getString(VcfRepository.REF),
				entity.getString(VcfRepository.ALT));
		return Collections.<Entity> singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	private void getTabixReaders() throws IOException
	{
		if (tabixReaders == null)
		{
			synchronized (this)
			{
				if (tabixReaders == null)
				{
					tabixReaders = new HashMap<>();
					String chroms = "1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|X|Y"; // yes, 1KG has Y

					for (String chr : chroms.split("\\|"))
					{
						String thGenChrom = new String(
								molgenisSettings.getProperty(THGEN_DIRECTORY_LOCATION_PROPERTY)
										+ File.separator
										+ (chr.equals("X") ? "ALL.chrX.phase3_shapeit2_mvncall_integrated.20130502.genotypes.vcf.gz" : chr
												.equals("Y") ? "ALL.chrY.phase3_integrated.20130502.genotypes.vcf.gz" : "ALL.chr"
												+ chr
												+ ".phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz"));
						if (new File(thGenChrom).exists())
						{
							TabixReader tr = new TabixReader(thGenChrom);
							tabixReaders.put(chr, tr);
						}
						else
						{
							LOG.info("No file found for path: " + thGenChrom);
						}
					}
				}
			}
		}
	}

	private synchronized Map<String, Object> annotateWith1000G(String chromosome, Long position, String reference,
			String alternative) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<>();

		if (!tabixReaders.containsKey(chromosome))
		{
			LOG.info("No chromosome " + chromosome + " in data!");
			return resultMap;
		}

		TabixReader.Iterator tabixIterator = tabixReaders.get(chromosome).query(
				chromosome + ":" + position + "-" + position);
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
			LOG.info("No data for CHROM: " + chromosome + " POS: " + position + " REF: " + reference + " ALT: "
					+ alternative + " LINE: " + line);
		}

		// if nothing found, return empty list for no hit
		if (line == null)
		{
			return resultMap;
		}

		// sanity check on content of line
		String[] split = null;
		split = line.split("\t", -1);
		if (split.length < 1000) // lots of data expected!
		{
			LOG.error("Bad 1000G data (split was < 1000 elements) for CHROM: " + chromosome + " POS: " + position
					+ " REF: " + reference + " ALT: " + alternative + " LINE: "
					+ line.substring(0, (line.length() > 250 ? 250 : line.length())));
			throw new IOException("Less than 1000 items found, Bad 1000G data.");
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
					LOG.error("Could not get MAF for line \n"
							+ line.substring(0, (line.length() > 250 ? 250 : line.length())));
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

		resultMap.put(THGEN_MAF, maf);
		return resultMap;
	}

	@Override
	public List<AttributeMetaData> getOutputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();

		metadata.add(new DefaultAttributeMetaData(THGEN_MAF, FieldTypeEnum.DECIMAL).setLabel(THGEN_MAF_LABEL));

		return metadata;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return AnnotatorInfo.create(Status.INDEV, Type.UNUSED, "unknown", "no description", getOutputMetaData());
	}

}
