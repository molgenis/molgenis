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
 * 
 * TODO: parse multiple alternative allele frequencies, test, polish, etc
 * 
 * 10	75832614	.	C	T,A,G		->		AF=5.848e-04,1.647e-05,4.118e-05
 * 
 * 
 * */
@Component("exacService")
public class ExACServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(ExACServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;


	public static final String EXAC_MAF = "EXACMAF";

	private static final String NAME = "EXAC";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ EXAC_MAF
							+ ",Number=1,Type=Float,Description=\"ExAC minor allele frequency. Taken straight for AF field, inverted when alleles are swapped\">"
							});

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

		//tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
		checkTabixReader();
		
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkInput(inputVcfFile, outputVCFWriter, infoFields, EXAC_MAF);

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
		return false;//Returns null -> new File(molgenisSettings.getProperty(EXAC_VCFGZ_LOCATION)).exists();
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
		Map<String, Object> resultMap = annotateEntityWithExAC(chromosome, entity.getLong(POSITION),
				entity.getString(REFERENCE), entity.getString(ALTERNATIVE));
		return Collections.<Entity> singletonList(getAnnotatedEntity(entity, resultMap));
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
		Double maf = null;

		TabixReader.Iterator tabixIterator = tabixReader.query(chromosome + ":" + position + "-" + position);

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
					LOG.error("Bad ExAC data (split was not 8 elements) for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
					continue;
				}
				
				LOG.info("ExAC variant found for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
						+ " ALT: " + alternative + " LINE: " + line);
				
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
							LOG.error("Bad number: " + info.replace("AF=", "") + " for line \n" + line);
						}
					}
				
				}
				
				if(maf == -1)
				{
					LOG.error("Bad 1000G data (no AF field) for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
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
					LOG.info("ExAC variant found [swapped MAF by 1-MAF!] for CHROM: " + chromosome + " POS: " + position
							+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
					
					maf = 1-maf; //swap MAF in this case!
					done = true;
				}
				else
				{
					if (i > 1)
					{
						LOG.warn("More than 1 hit in the ExAC! for CHROM: " + chromosome + " POS: " + position
								+ " REF: " + reference + " ALT: " + alternative);
					}
					else
					{
						LOG.info("ExAC variant position found but ref/alt not matched! for CHROM: " + chromosome + " POS: " + position
								+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
					}
					
				}
			}
			else
			{
				LOG.warn("No hit found in ExAC for CHROM: " + chromosome + " POS: " + position + " REF: "
						+ reference + " ALT: " + alternative);
				done = true;
			}
		}

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(EXAC_MAF, maf);
		return resultMap;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(EXAC_MAF, FieldTypeEnum.DECIMAL));

		return metadata;
	}

}
