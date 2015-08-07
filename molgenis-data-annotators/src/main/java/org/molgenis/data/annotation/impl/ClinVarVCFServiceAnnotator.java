package org.molgenis.data.annotation.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.entity.AnnotatorInfo;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Status;
import org.molgenis.data.annotation.entity.AnnotatorInfo.Type;
import org.molgenis.data.annotation.settings.AnnotationInMemorySettings;
import org.molgenis.data.annotation.settings.AnnotationSettings;
import org.molgenis.data.annotation.utils.AnnotatorUtils;
import org.molgenis.data.annotator.tabix.TabixReader;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("clinvarVcfService")
public class ClinVarVCFServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(ClinVarVCFServiceAnnotator.class);

	private static final String NAME = "ClinvarVCF";

	public final static String CLINVAR_CLINSIG_LABEL = "CLINVAR_CLNSIG";
	public final static String CLINVAR_CLINSIG = VcfRepository.getInfoPrefix() + CLINVAR_CLINSIG_LABEL;
	private volatile TabixReader tabixReader;

	final List<String> infoFields = Arrays.asList(new String[]
	{ "##INFO=<ID=" + CLINVAR_CLINSIG.substring(VcfRepository.getInfoPrefix().length())
			+ ",Number=1,Type=String,Description=\"ClinVar clinical significance\">" });

	private final AnnotationSettings annotationSettings;

	@Autowired
	public ClinVarVCFServiceAnnotator(AnnotationSettings annotationSettings)
	{
		this.annotationSettings = checkNotNull(annotationSettings);
	}

	public ClinVarVCFServiceAnnotator(File clinvarVcfFileLocation, File inputVcfFile, File outputVCFFile)
			throws Exception
	{
		this.annotationSettings = new AnnotationInMemorySettings();
		annotationSettings.setClinVarLocation(clinvarVcfFileLocation.getAbsolutePath());

		checkTabixReader();

		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, getOutputMetaData(),
				CLINVAR_CLINSIG.substring(VcfRepository.getInfoPrefix().length()));

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
	public String getSimpleName()
	{
		return NAME;
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
					tabixReader = new TabixReader(annotationSettings.getClinVarLocation());
				}
			}
		}
	}

	@Override
	protected boolean annotationDataExists()
	{
		return new File(annotationSettings.getClinVarLocation()).exists();
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		checkTabixReader();

		Map<String, Object> resultMap = annotateEntityWithClinVar(entity.getString(VcfRepository.CHROM),
				entity.getLong(VcfRepository.POS), entity.getString(VcfRepository.REF),
				entity.getString(VcfRepository.ALT));
		return Collections.singletonList(AnnotatorUtils.getAnnotatedEntity(this, entity, resultMap));
	}

	private synchronized Map<String, Object> annotateEntityWithClinVar(String chromosome, Long position,
			String reference, String alternative) throws IOException
	{
		TabixReader.Iterator tabixIterator = null;
		try
		{
			tabixIterator = tabixReader.query(chromosome + ":" + position + "-" + position);
		}
		catch (Exception e)
		{
			LOG.error(
					"Something went wrong (chromosome not in data?) when querying ClinVar tabix file for " + chromosome
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
			LOG.error("Bad ClinVar data (split was not 8 elements) for CHROM: " + chromosome + " POS: " + position
					+ " REF: " + reference + " ALT: " + alternative + " LINE: " + line);
			throw new IOException("Bad data! see log");
		}

		// match ref & alt
		if (split[3].equals(reference) && split[4].equals(alternative))
		{
			LOG.info("ClinVar variant found for CHROM: " + chromosome + " POS: " + position + " REF: " + reference
					+ " ALT: " + alternative);
			// ...CLNORIGIN=1;CLNSRCID=.;CLNSIG=2;CLNDSDB=MedGen;CLNDSDBID=CN169374;CLNDBN=not_specified;... etc
			String clinSig = null;
			String[] infoSplit = split[7].split(";", -1);
			for (String infoField : infoSplit)
			{
				if (infoField.startsWith("CLNSIG="))
				{
					clinSig = infoField.replace("CLNSIG=", "");
					resultMap.put(CLINVAR_CLINSIG, clinSig);
				}
			}
		}

		return resultMap;
	}

	@Override
	public List<AttributeMetaData> getOutputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();
		metadata.add(
				new DefaultAttributeMetaData(CLINVAR_CLINSIG, FieldTypeEnum.STRING).setLabel(CLINVAR_CLINSIG_LABEL));
		return metadata;
	}

	@Override
	public AnnotatorInfo getInfo()
	{
		return AnnotatorInfo.create(Status.BETA, Type.PHENOTYPE_ASSOCIATION, "clinvar", "", getOutputMetaData());
	}

}
