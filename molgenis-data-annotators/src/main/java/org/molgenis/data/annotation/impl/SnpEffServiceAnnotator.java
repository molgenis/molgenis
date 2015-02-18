package org.molgenis.data.annotation.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.TabixReader;
import org.molgenis.data.annotation.VariantAnnotator;
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
 * 
 * new ANN field replacing EFF:
 * 
 * ANN=A|missense_variant|MODERATE|NEXN|NEXN|transcript|NM_144573.3|Coding|8/13|c.733G>A|p.Gly245Arg|1030/3389|733/2028|245/675||
 * 
 * 
 * -lof doesnt seem to work? would be great... http://snpeff.sourceforge.net/snpEff_lof_nmd.pdf
 * 
 * 
 * */
@Component("gonlService")
public class SnpEffServiceAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(SnpEffServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	// must be compatible with VCF format, ie no funny characters
	public static final String SNPEFF_EFF = "ANN";
	private static final String NAME = "SnpEff";
	public static final String SNPEFF_PATH = "snpeff_path";
	
	HashMap<String, TabixReader> tabixReaders = null;

	@Autowired
	public SnpEffServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public SnpEffServiceAnnotator(File snpEffLocation, File inputVcfFile, File outputVCFFile) throws Exception
	{
		Process p = Runtime.getRuntime().exec("java -jar \"" + snpEffLocation + "\"");
		BufferedInputStream pOutput= new BufferedInputStream(p.getInputStream());
		synchronized (p) {
			   p.waitFor();
			}
		
		int read = 0;
		byte[] output = new byte[1024];
		
		System.out.printf("Testing if SnpEff can be ran from " + snpEffLocation + " ...");
		while ((read = pOutput.read(output)) != -1) {
		    System.out.println(output[read]);
		}
		
		if(p.exitValue() != 0)
		{
			LOG.error("SnpEff not runnable from location " + snpEffLocation + " !");
			
		}
		else{
			LOG.info("Exit value 0, all is well...");
		}
		
		this.molgenisSettings = new MolgenisSimpleSettings();
		molgenisSettings.setProperty(SNPEFF_PATH, snpEffLocation.getAbsolutePath());

		this.annotatorService = new AnnotationServiceImpl();

		//tabixReader = new TabixReader(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY));
		checkSnpEffPath();
		
		
		//java -Xmx2g -jar /gcc/resources/snpEff/3.6c/snpEff.jar hg19 -v -canon -ud 0 -spliceSiteSize 5 nc_SNPs.vcf > nc_SNPs_snpeff_no_ud_ss5bp_canon_out.txt
		
		
		//Process process = new ProcessBuilder("path/to/myexe.exe","param1","param2").start();
		Process process = new ProcessBuilder("java -Xmx2g -jar "+SNPEFF_PATH+" hg19 -v -lof -canon -ud 0 -spliceSiteSize 5 "+inputVcfFile+" > " + outputVCFFile).start();
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line;

		System.out.printf("Output of running SnpEff is:");

		while ((line = br.readLine()) != null) {
		  System.out.println(line);
		}
		
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
		return new File(molgenisSettings.getProperty(SNPEFF_PATH)).exists();
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		checkSnpEffPath();

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
	private void checkSnpEffPath() throws IOException
	{
		File snpEffpath = new File(molgenisSettings.getProperty(SNPEFF_PATH));
		if(snpEffpath.exists() && snpEffpath.isFile())
		{
			LOG.info("SnpEff found at + " + snpEffpath.getAbsolutePath());
		}
		else{
			LOG.equals("SnpEff NOT found at + " + snpEffpath.getAbsolutePath());
			throw new IOException("File not found " + snpEffpath.getAbsolutePath());
		}
	}

	private synchronized Map<String, Object> annotateEntityWithGoNL(String chromosome, Long position, String reference,
			String alternative) throws IOException
	{
		return null;
		//
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(SNPEFF_EFF, FieldTypeEnum.STRING)); //FIXME: correct type?

		return metadata;
	}

}
