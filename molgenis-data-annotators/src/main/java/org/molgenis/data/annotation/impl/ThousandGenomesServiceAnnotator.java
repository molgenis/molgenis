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
 * 
 * TODO: test and polish
 * 
 * 
 * X	47065383	rs200383898	C	A	100	PASS	AC=1;AF=0.000264901;AN=3775;NS=2504;DP=14025;AMR_AF=0;AFR_AF=0.0008;EUR_AF=0;SAS_AF=0;EAS_AF=0;AA=C|||	GT	0	0|0	0|0	[+2500 more samples]
 * 
 * 
 * TODO: multiple alternative alleles!!!
 * 
 * ERROR o.m.d.a.i.ThousandGenomesServiceAnnotator - Bad 1000G data (no AF field) for CHROM: 2 POS: 179631362 REF: A ALT: C LINE: 2	179631362	rs3816782	A	AAAC,C	100	PASS	AC=3,728;AF=0.000599042,0.145367;AN=5008;NS=2504;DP=19666;EAS_AF=0,0.1954;AMR_AF=0,0.245;AFR_AF=0.0023,0.1611;EUR_AF=0,0.0765;SAS_AF=0,0.0726
 * 
 * */
@Component("thousandGenomesService")
public class ThousandGenomesServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(ThousandGenomesServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	public static final String THGEN_MAF = "1000GMAF";

	private static final String NAME = "1000G";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ THGEN_MAF
							+ ",Number=1,Type=Float,Description=\"1000G minor allele frequency.\">"
							});

	public static final String THGEN_DIRECTORY_LOCATION_PROPERTY = "1000G_location";
	
	HashMap<String, TabixReader> tabixReaders = null;

	@Autowired
	public ThousandGenomesServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public ThousandGenomesServiceAnnotator(File thGenDir, File inputVcfFile, File outputVCFFile) throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(THGEN_DIRECTORY_LOCATION_PROPERTY, thGenDir.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		//tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
		checkTabixReader();
		
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkInput(inputVcfFile, outputVCFWriter, infoFields, THGEN_MAF);

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
		return new File(molgenisSettings.getProperty(THGEN_DIRECTORY_LOCATION_PROPERTY)).exists();
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
		Map<String, Object> resultMap = annotateEntityWith1000G(chromosome, entity.getLong(POSITION),
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
					
					String chroms = "1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|X|Y"; //yes, 1KG has Y
					
				//	tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
					
					for(String chr : chroms.split("\\|"))
					{
						String thGenChrom = new String(
								molgenisSettings.getProperty(THGEN_DIRECTORY_LOCATION_PROPERTY) + File.separator + 
								(chr.equals("X") ? "ALL.chrX.phase3_shapeit2_mvncall_integrated.20130502.genotypes.vcf.gz" : chr.equals("Y") ? "ALL.chrY.phase3_integrated.20130502.genotypes.vcf.gz" : "ALL.chr"+chr+".phase3_shapeit2_mvncall_integrated_v5.20130502.genotypes.vcf.gz")
								);
						TabixReader tr = new TabixReader(thGenChrom);
						tabixReaders.put(chr, tr);
					}
				}
			}
		}
	}

	private synchronized Map<String, Object> annotateEntityWith1000G(String chromosome, Long position, String reference,
			String alternative) throws IOException
	{
		Double maf = null;

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
						+ " ALT: " + alternative + " LINE: " + line.substring(0, (line.length() > 250 ? 250 : line.length())));
				throw sfx;
			}

			if (line != null)
			{
				String[] split = null;
				i++;
				split = line.split("\t");
				if (split.length < 1000) //lots of data expected
				{
					LOG.error("Bad 1000G data (split was < 1000 elements) for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line.substring(0, (line.length() > 250 ? 250 : line.length())));
					continue;
				}
				
				LOG.info("1000G variant found for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
						+ " ALT: " + alternative + " LINE: " + line.substring(0, (line.length() > 250 ? 250 : line.length())));
				
				String[] infoFields = split[7].split(";", -1);
			
				for(String info : infoFields)
				{
					if(info.startsWith("AF="))
					{
						try{
							maf = Double.parseDouble(info.replace("AF=", ""));
							break;
						}catch( java.lang.NumberFormatException e)
						{
							LOG.error("Bad number: " + info.replace("AF=", "") + " for line \n" + line.substring(0, (line.length() > 250 ? 250 : line.length())));
						}
					}
					
				}
				
				if(maf == null)
				{
					LOG.error("Bad 1000G data (no AF field) for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line.substring(0, (line.length() > 250 ? 250 : line.length())));
					continue;
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
					LOG.info("1000G variant found [swapped MAF by 1-MAF!] for CHROM: " + chromosome + " POS: " + position
							+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line.substring(0, (line.length() > 250 ? 250 : line.length())));
					
					maf = 1-maf; //swap MAF in this case!
					done = true;
				}
				else
				{
					if (i > 1)
					{
						LOG.warn("More than 1 hit in the 1000G! for CHROM: " + chromosome + " POS: " + position
								+ " REF: " + reference + " ALT: " + alternative);
					}
					else
					{
						LOG.info("1000G variant position found but ref/alt not matched! for CHROM: " + chromosome + " POS: " + position
								+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line.substring(0, (line.length() > 250 ? 250 : line.length())));
					}
					
				}
			}
			else
			{
				LOG.warn("No hit found in 1000G for CHROM: " + chromosome + " POS: " + position + " REF: "
						+ reference + " ALT: " + alternative);
				done = true;
			}
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(THGEN_MAF, maf);
		return resultMap;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(THGEN_MAF, FieldTypeEnum.DECIMAL));

		return metadata;
	}

}
