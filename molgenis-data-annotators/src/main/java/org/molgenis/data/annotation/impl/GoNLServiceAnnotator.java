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
import java.util.concurrent.ConcurrentHashMap;

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

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

/**
 * TODO: test and polish
 * */
@Component("gonlService")
public class GoNLServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(GoNLServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	public static final String GONL_MAF = "GONLMAF";
	public static final String GONL_GTC = "GONLGTC";

	private static final String NAME = "GONL";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ GONL_MAF
							+ ",Number=1,Type=Float,Description=\"GoNL minor allele frequency. Calculated by dividing AC by AN. For example: AC=23;AN=996 = 0.02309237\">",
					"##INFO=<ID="
							+ GONL_GTC
							+ ",Number=G,Type=Integer,Description=\"GoNL genotype counts. For example: GONLGTC=69,235,194. Listed in the same order as the ALT alleles in case multiple ALT alleles are present = 0/0,0/1,1/1,0/2,1/2,2/2,0/3,1/3,2/3,3/3,etc. Phasing is ignored; hence 0/1, 1/0, 0|1 and 1|0 are all counted as 0/1. Incomplete gentotypes (./., ./0, ./1, ./2, etc.) are completely discarded for calculating GTC.\">"
							});

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

		//tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
		checkTabixReader();
		
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkInput(inputVcfFile, outputVCFWriter, infoFields, GONL_MAF);

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
		checkTabixReader();

		// FIXME need to solve this! duplicate notation for CHROM in VcfRepository.CHROM and LocusAnnotator.CHROMOSOME
		String chromosome = entity.getString(VcfRepository.CHROM) != null ? entity.getString(VcfRepository.CHROM) : entity
				.getString(CHROMOSOME);

		// FIXME use VcfRepository.POS, use VcfRepository.REF, use VcfRepository.ALT ?
		Map<String, Object> resultMap = annotateEntityWithGoNL(chromosome, entity.getLong(POSITION),
				entity.getString(REFERENCE), entity.getString(ALTERNATIVE));
		return Collections.<Entity> singletonList(getAnnotatedEntity(entity, resultMap));
	}

	/**
	 * Makes sure the tabixReader exists.
	 */
	private void checkTabixReader() throws IOException
	{
		if (tabixReaders == null)
		{
			synchronized (this)
			{
				if (tabixReaders == null)
				{
					tabixReaders = new HashMap<String, TabixReader>();
					
					String chroms = "1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|X";
				//	tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
					
					for(String chr : chroms.split("\\|"))
					{
						String gonlchrom = new String(
								molgenisSettings.getProperty(GONL_DIRECTORY_LOCATION_PROPERTY) + File.separator
										//small hack to use chrX from release 4.. (5 isn't out yet!)
										+ (!chr.equals("X") ? "gonl.chr"+chr+".snps_indels.r5.vcf.gz" : "gonl.chrX.release4.gtc.vcf.gz")
								);
						TabixReader tr = new TabixReader(gonlchrom);
						tabixReaders.put(chr, tr);
					}
				}
			}
		}
	}

	private synchronized Map<String, Object> annotateEntityWithGoNL(String chromosome, Long position, String reference,
			String alternative) throws IOException
	{
		Double maf = null;
		String gtc = null;

		TabixReader.Iterator tabixIterator = tabixReaders.get(chromosome).query(chromosome + ":" + position + "-" + position);

		// TabixReaderIterator does not have a hasNext();
		boolean done = tabixIterator == null;
		int i = 0;

		while (!done)
		{
			String line = null;
			try{
				line = tabixIterator.next();
			}
			catch(net.sf.samtools.SAMFormatException sfx)
			{
				LOG.error("Bad GZIP file for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
						+ " ALT: " + alternative + " LINE: " + line);
				throw sfx;
			}
			
			//typical line:
			//22      16055070        rs4389403       G       A       .       PASS    AC=93;AN=996;DB;GTC=405,93,0;set=SNP
			//
			//but sometimes without DB:
			//22      16053249        .       C       T       .       PASS    AC=3;AN=996;GTC=495,3,0;set=SNP

			if (line != null)
			{
				String[] split = null;
				i++;
				split = line.split("\t");
				if (split.length != 8)
				{
					LOG.error("Bad GoNL data (split was not 8 elements) for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
					continue;
				}
				
				LOG.info("GoNL variant found for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
						+ " ALT: " + alternative + " LINE: " + line);
				
				String[] infoFields = split[7].split(";", -1);
				double ac = -1;
				double an = -1;
				
				for(String info : infoFields)
				{
					if(info.startsWith("AC="))
					{
						ac = Double.parseDouble(info.replace("AC=", ""));
					}
					if(info.startsWith("AN="))
					{
						an = Double.parseDouble(info.replace("AN=", ""));
					}
					if(info.startsWith("GTC="))
					{
						gtc = info.replace("GTC=", "");
					}
				}
				
				if(ac!=-1 && an!= -1)
				{
					maf = ac/an;
				}
				else
				{
					LOG.error("Bad GoNL data (no AC or AN info field) for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
					continue;
				}
				
				if(gtc == null)
				{
					LOG.error("Bad GoNL data (no GTC info field) for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
				}

					
				
				if (split[3].equals(reference) && split[4].equals(alternative))
				{
					//all fine
					done = true;
				}
				// In some cases, the ref and alt are swapped. If this is the case, the initial if statement above will
				// fail, we can just check whether such a swapping has occured
				else if (split[4].equals(reference) && split[3].equals(alternative))
				{
					LOG.info("GoNL variant found [swapped MAF by 1-MAF!] for CHROM: " + chromosome + " POS: " + position
							+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
					
					maf = 1-maf; //swap MAF in this case!
					done = true;
				}
				else
				{
					if (i > 1)
					{
						LOG.warn("More than 1 hit in the GoNL! for CHROM: " + chromosome + " POS: " + position
								+ " REF: " + reference + " ALT: " + alternative);
					}
					else
					{
						LOG.info("GoNL variant position found but ref/alt not matched! for CHROM: " + chromosome + " POS: " + position
								+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
					}
					
				}
			}
			else
			{
				LOG.warn("No hit found in GoNL for CHROM: " + chromosome + " POS: " + position + " REF: "
						+ reference + " ALT: " + alternative);
				done = true;
			}
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(GONL_MAF, maf);
		resultMap.put(GONL_GTC, gtc);
		return resultMap;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GONL_MAF, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(GONL_GTC, FieldTypeEnum.STRING)); //FIXME: correct type?

		return metadata;
	}

}
