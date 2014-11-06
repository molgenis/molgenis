package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.annotation.TabixReader;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.omx.MolgenisSimpleSettings;
import org.molgenis.vcf.VcfRecord;
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
	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	static final String CADD_SCALED = "CADD_SCALED";
	static final String CADD_ABS = "CADD_ABS";

	private static final String NAME = "CADD";

	public static final String CADD_FILE_LOCATION_PROPERTY = "cadd_location";
	TabixReader tr;

	@Autowired
	public CaddServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;

		tr = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
	}

	public CaddServiceAnnotator(File caddTsvGzFile, File inputVcfFile, File outputVCFFile, boolean errorOnMissing)
			throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(CADD_FILE_LOCATION_PROPERTY, caddTsvGzFile.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		tr = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();
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
	public String getName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		List<Entity> results = new ArrayList<Entity>();

		String chromosome = entity.getString(VCF_CHROMOSOME);
		Long position = entity.getLong(POSITION);
		String reference = entity.getString(REFERENCE);
		String alternative = entity.getString(ALTERNATIVE);

		String caddAbs = null;
		String caddScaled = null;

		String next;
		TabixReader.Iterator caddIterator = tr.query(chromosome + ":" + position + "-" + position);
		while (caddIterator != null && (next = caddIterator.next()) != null)
		{
			String[] split = next.split("\t", -1);

			if (split[2].equals(reference) && split[3].equals(alternative))
			{
				caddAbs = split[4];
				caddScaled = split[5];
			}
			// In some cases, the ref and alt are swapped. If this is the case,
			// the initial if statement above will
			// fail, we can just check whether such a swapping has occured
			else if (split[3].equals(reference) && split[2].equals(alternative))
			{
				caddAbs = split[4];
				caddScaled = split[5];
			}
			// If both matchings are incorrect, there is something really wrong
			// with the source files,
			// which we cant do anything about.
			else
			{
				//
			}
		}

		HashMap<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put(CADD_ABS, caddAbs);
		resultMap.put(CADD_SCALED, caddScaled);
		resultMap.put(CHROMOSOME, chromosome);
		resultMap.put(POSITION, position);
		resultMap.put(ALTERNATIVE, alternative);
		resultMap.put(REFERENCE, reference);

		results.add(new MapEntity(resultMap));

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

	public static void main(String[] args) throws Exception
	{

		if (args.length != 4)
		{
			throw new Exception(
					"Usage: java -Xmx4g -jar CADD.jar [CADD *.tsv.gz file] [input VCF] [output VCF] [error on missing CADD score T/F].\n");
		}

		File caddTsvGzFile = new File(args[0]);
		if (!caddTsvGzFile.exists())
		{
			throw new Exception("CADD *.tsv.gz file not found at " + caddTsvGzFile);
		}
		if (caddTsvGzFile.isDirectory())
		{
			throw new Exception("CADD *.tsv.gz file is a directory, not a file!");
		}

		File inputVcfFile = new File(args[1]);
		if (!inputVcfFile.exists())
		{
			throw new Exception("Input VCF file not found at " + inputVcfFile);
		}
		if (inputVcfFile.isDirectory())
		{
			throw new Exception("Input VCF file is a directory, not a file!");
		}

		File outputVCFFile = new File(args[2]);
		if (outputVCFFile.exists())
		{
			throw new Exception("Output VCF file already exists at " + outputVCFFile.getAbsolutePath());
		}

		String errorOnMissing = new String(args[3]);
		if (!errorOnMissing.matches("T|F"))
		{
			throw new Exception("Error on missing must match T or F.");
		}
		boolean errorOnMissingBool = errorOnMissing.equals("T") ? true : false;

		// engage!
		new CaddServiceAnnotator(caddTsvGzFile, inputVcfFile, outputVCFFile, errorOnMissingBool);

	}

}
