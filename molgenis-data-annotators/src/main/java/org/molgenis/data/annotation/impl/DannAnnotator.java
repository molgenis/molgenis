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

import net.sf.samtools.SAMFormatException;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
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

@Component("dannService")
public class DannAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(DannAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	public static final String DANN_LABEL = "DANN";
	public static final String DANN = VcfRepository.getInfoPrefix() + "DANN";

	private static final String NAME = "DANN";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{ "##INFO=<ID="
					+ DANN.substring(VcfRepository.getInfoPrefix().length())
					+ ",Number=1,Type=Float,Description=\"DANN score. See 1. Quang, D., Chen, Y. & Xie, X. DANN: a deep learning approach for annotating the pathogenicity of genetic variants. Bioinformatics 31, 761–763 (2014) @ http://bioinformatics.oxfordjournals.org/cgi/doi/10.1093/bioinformatics/btu703." });

	public static final String DANN_FILE_LOCATION_PROPERTY = "dann_location";

	private volatile TabixReader tabixReader;

	@Autowired
	public DannAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService) throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public DannAnnotator(File dannTsvGzFile, File inputVcfFile, File outputVCFFile) throws Exception
	{
		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(DANN_FILE_LOCATION_PROPERTY, dannTsvGzFile.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		tabixReader = new TabixReader(molgenisSettings.getProperty(DANN_FILE_LOCATION_PROPERTY));
		checkTabixReader();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields,
				DANN.substring(VcfRepository.getInfoPrefix().length()));

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

	private synchronized Map<String, Object> annotateEntityWithDann(String chromosome, Long position, String reference,
			String alternative) throws IOException, InterruptedException
	{
		TabixReader.Iterator tabixIterator = null;
		try
		{
			tabixIterator = tabixReader.query(chromosome + ":" + position + "-" + position);
		}
		catch (Exception e)
		{
			LOG.error("Something went wrong (chromosome not in data?) when querying DANN tabix file for " + chromosome
					+ " POS: " + position + " REF: " + reference + " ALT: " + alternative + "! skipping...");
		}

		Double dann = null;

		// TabixReaderIterator does not have a hasNext();
		boolean done = tabixIterator == null;

		int i = 0;

		Map<String, Object> resultMap = new HashMap<String, Object>();

		// get line(s) from data, we expect 0 (no hit), 1 (specialized files such as 1000G) or 3 (whole genome file), so
		// 0 to 3 hits
		while (!done)
		{
			String line = null;

			try
			{
				line = tabixIterator.next();
				LOG.info("Currently processing tabix query result: " + line);
			}
			catch (SAMFormatException sfx)
			{
				LOG.error("Bad GZIP file for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
						+ " ALT: " + alternative + " LINE: " + line);
				throw sfx;
			}
			catch (NullPointerException npe)
			{
				LOG.info("No data for CHROM: " + chromosome + " POS: " + position + " REF: " + reference + " ALT: "
						+ alternative + " LINE: " + line);
			}

			if (line != null)
			{
				String[] split = null;
				i++;
				split = line.split("\t");
				if (split.length != 5)
				{
					LOG.error("bad DANN output for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
					continue;
				}
				if (split[2].equals(reference) && split[3].equals(alternative))
				{

					try
					{
						dann = Double.parseDouble(split[4]);

						done = true;
					}
					catch (NumberFormatException nfe)
					{
						LOG.info("NumberFormatException for line: " + chromosome + " POS: " + position + " REF: "
								+ reference + " ALT: " + alternative + " LINE: " + line);

						
					}

				}
				// In some cases, the ref and alt are swapped. If this is the case, the initial if statement above will
				// fail, we can just check whether such a swapping has occurred
				else if (split[3].equals(reference) && split[2].equals(alternative))
				{
					LOG.info("DANN scores found [swapped REF and ALT!] for CHROM: " + chromosome + " POS: " + position
							+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
					try
					{
						dann = Double.parseDouble(split[4]);

						done = true;
					}
					catch (NumberFormatException nfe)
					{
						LOG.info("NumberFormatException for line: " + chromosome + " POS: " + position + " REF: "
								+ reference + " ALT: " + alternative + " LINE: " + line);
						
						
					}
				}
				else
				{
					if (i > 3)
					{
						LOG.warn("More than 3 hits in the DANN file! for CHROM: " + chromosome + " POS: " + position
								+ " REF: " + reference + " ALT: " + alternative);
					}
				}
			}
			else
			// case: line == null
			{
				LOG.warn("No hit found in DANN file for CHROM: " + chromosome + " POS: " + position + " REF: "
						+ reference + " ALT: " + alternative);
				done = true;
				
				
			}

		}

		resultMap.put(DANN, dann);

		return resultMap;
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		DefaultAttributeMetaData dann = new DefaultAttributeMetaData(DANN, FieldTypeEnum.DECIMAL)
				.setLabel(DANN_LABEL)
				.setDescription(
						"DANN is a tool for estimating the probability that a point mutation at a position in "
								+ "the genome will cause deleterious effects."
								+ "(source: http://bioinformatics.oxfordjournals.org/cgi/doi/10.1093/bioinformatics/btu703");

		metadata.addAttributeMetaData(dann);

		return metadata;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		checkTabixReader();
		Map<String, Object> resultMap = annotateEntityWithDann(entity.getString(VcfRepository.CHROM),
				entity.getLong(VcfRepository.POS), entity.getString(VcfRepository.REF),
				entity.getString(VcfRepository.ALT));
		return Collections.<Entity> singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public boolean annotationDataExists()
	{
		return new File(molgenisSettings.getProperty(DANN_FILE_LOCATION_PROPERTY)).exists();
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
					tabixReader = new TabixReader(molgenisSettings.getProperty(DANN_FILE_LOCATION_PROPERTY));
				}
			}
		}
	}

}
