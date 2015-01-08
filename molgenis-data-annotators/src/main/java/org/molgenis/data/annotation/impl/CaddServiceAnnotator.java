package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This class performs a system call to cross reference a chromosome and genomic location with a tabix indexed file. A
 * match can result in 1, 2 or 3 hits. These matches are reduced to one based on a reference and alternative nucleotide
 * base. The remaining hit will be used to parse two CADD scores.
 * </p>
 * 
 * <p>
 * <b>CADD returns:</b> CADD score Absolute, CADD score Scaled
 * </p>
 * 
 * @author mdehaan
 * 
 * */
@Component("caddService")
public class CaddServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(CaddServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	public static final String CADD_SCALED = "CADDSCALED";
	public static final String CADD_ABS = "CADDABS";

	private static final String NAME = "CADD";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ CADD_SCALED
							+ ",Number=1,Type=Float,Description=\"CADD scaled C score, ie. phred-like. See Kircher et al. 2014 (http://www.ncbi.nlm.nih.gov/pubmed/24487276) or CADD website (http://cadd.gs.washington.edu/) for more information.\">",
					"##INFO=<ID="
							+ CADD_ABS
							+ ",Number=1,Type=Float,Description=\"CADD absolute C score, ie. unscaled SVM output. Useful as  reference when the scaled score may be unexpected.\">" });

	public static final String CADD_FILE_LOCATION_PROPERTY = "cadd_location";
	TabixReader tabixReader;

	@Autowired
	public CaddServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public CaddServiceAnnotator(File caddTsvGzFile, File inputVcfFile, File outputVCFFile) throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(CADD_FILE_LOCATION_PROPERTY, caddTsvGzFile.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkInput(inputVcfFile, outputVCFWriter, infoFields, CADD_SCALED);

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
		return new File(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY)).exists();
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		if (tabixReader == null)
		{
			tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
		}
		boolean done;
		int i;
		List<Entity> results = new ArrayList<Entity>();
		// FIXME need to solve this! duplicate notation for CHROM in VcfRepository.CHROM and LocusAnnotator.CHROMOSOME
		String chromosome = entity.getString(VcfRepository.CHROM) != null ? entity.getString(VcfRepository.CHROM) : entity
				.getString(CHROMOSOME);

		Long position = entity.getLong(POSITION); // FIXME use VcfRepository.POS ?
		String reference = entity.getString(REFERENCE); // FIXME use VcfRepository.REF ?
		String alternative = entity.getString(ALTERNATIVE); // FIXME use VcfRepository.ALT ?

		Double caddAbs = null;
		Double caddScaled = null;

		TabixReader.Iterator tabixIterator = tabixReader.query(chromosome + ":" + position + "-" + position);
		if (tabixIterator == null)
		{
			System.out.print("");
		}
		// TabixReaderIterator does not have a hasNext();
		done = false;
		i = 0;
		while (!done)
		{
			String line = tabixIterator.next();

			if (line != null)
			{
				String[] split = null;
				i++;
				split = line.split("\t");
				if (split.length != 6)
				{
					LOG.error("bad CADD output for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
					continue;
				}
				if (split[2].equals(reference) && split[3].equals(alternative))
				{
					LOG.info("CADD scores found for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
							+ " ALT: " + alternative + " LINE: " + line);
					caddAbs = Double.parseDouble(split[4]);
					caddScaled = Double.parseDouble(split[5]);
					done = true;
				}
				// In some cases, the ref and alt are swapped. If this is the case, the initial if statement above will
				// fail, we can just check whether such a swapping has occured
				else if (split[3].equals(reference) && split[2].equals(alternative))
				{
					LOG.info("CADD scores found [swapped REF and ALT!] for CHROM: " + chromosome + " POS: " + position
							+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
					caddAbs = Double.parseDouble(split[4]);
					caddScaled = Double.parseDouble(split[5]);
					done = true;
				}
				else
				{
					if (i > 3)
					{
						LOG.warn("More than 3 hits in the CADD file! for CHROM: " + chromosome + " POS: " + position
								+ " REF: " + reference + " ALT: " + alternative);
					}
				}
			}
			else
			{
				LOG.warn("No hit found in CADD file for CHROM: " + chromosome + " POS: " + position + " REF: "
						+ reference + " ALT: " + alternative);
				done = true;
			}
		}

		HashMap<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put(CADD_ABS, caddAbs);
		resultMap.put(CADD_SCALED, caddScaled);

		results.add(getAnnotatedEntity(entity, resultMap));

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CADD_ABS, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CADD_SCALED, FieldTypeEnum.DECIMAL));

		return metadata;
	}

}
