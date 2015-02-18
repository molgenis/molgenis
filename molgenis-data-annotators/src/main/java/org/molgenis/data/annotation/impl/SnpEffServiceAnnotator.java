package org.molgenis.data.annotation.impl;

import java.io.*;
import java.util.*;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.AnnotationServiceImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.server.MolgenisSimpleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
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
@Component("SnpEffServiceAnnotator")
public class SnpEffServiceAnnotator implements RepositoryAnnotator,
        ApplicationListener<ContextRefreshedEvent> {
	private static final Logger LOG = LoggerFactory.getLogger(SnpEffServiceAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	public static final String SNPEFF_EFF = "ANN";
	private static final String NAME = "SnpEff";
	public static final String SNPEFF_PATH = "snpeff_path";

    public static final String REFERENCE = "REF";
    public static final String ALTERNATIVE = "ALT";
    public static final String CHROMOSOME = "#CHROM";
    public static final String POSITION = "POS";

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        annotatorService.addAnnotator(this);
    }

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

		if(!checkSnpEffPath()){
            throw new FileNotFoundException("SnpEff executable not found");
        }
		
		//java -Xmx2g -jar /gcc/resources/snpEff/3.6c/snpEff.jar hg19 -v -canon -ud 0 -spliceSiteSize 5 nc_SNPs.vcf > nc_SNPs_snpeff_no_ud_ss5bp_canon_out.txt
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
	public String getSimpleName()
	{
		return NAME;
	}

    @Override
    public String getFullName() {
        return getSimpleName();
    }

    @Override
    public String getDescription() {
        return "TODO: nice SnpEff description";
    }

    private boolean checkSnpEffPath()
	{
		return false;
        /*File snpEffpath = new File(molgenisSettings.getProperty(SNPEFF_PATH));
		if(snpEffpath.exists() && snpEffpath.isFile())
		{
			LOG.info("SnpEff found at + " + snpEffpath.getAbsolutePath());
            return true;
		}
		else{
			LOG.error("SnpEff NOT found at + " + snpEffpath.getAbsolutePath());
			return false;
		} **/
	}

    @Override
    public Iterator<Entity> annotate(Iterable<Entity> source) {
        String tempFileName = UUID.randomUUID().toString();
        File temp = null;
        try {
            temp = File.createTempFile(tempFileName, ".vcf");
            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            bw.write("This is the temporary file content");
            for(Entity entity : source){
                StringBuilder builder = new StringBuilder();
                builder.append(entity.getString(CHROMOSOME));
                builder.append(entity.getString("\t"));
                builder.append(entity.getString(POSITION));
                builder.append(entity.getString(".\t"));
                builder.append(entity.getString(REFERENCE));
                builder.append(entity.getString("\t"));
                builder.append(entity.getString(ALTERNATIVE));
                bw.write(builder.toString());
            }
            bw.close();
        } catch (Exception e) {
            //TODO clean error handling
            e.printStackTrace();
        }

        //execute SnpEff with file as input
        //output to temp directory?

        /**
         * Example process code from Python runner:
         *
         * 	public void executeScript(File script, PythonOutputHandler outputHandler)
         {
         // Check if r is installed
         File file = new File(pythonScriptExecutable);
         if (!file.exists())
         {
         throw new MolgenisPythonException("File [" + pythonScriptExecutable + "] does not exist");
         }

         // Check if r has execution rights
         if (!file.canExecute())
         {
         throw new MolgenisPythonException("Can not execute [" + pythonScriptExecutable
         + "]. Does it have executable permissions?");
         }

         // Check if the r script exists
         if (!script.exists())
         {
         throw new MolgenisPythonException("File [" + script + "] does not exist");
         }

         try
         {
         // Create r process
         LOG.info("Running python script [" + script.getAbsolutePath() + "]");
         Process process = Runtime.getRuntime().exec(pythonScriptExecutable + " " + script.getAbsolutePath());

         // Capture the error output
         final StringBuilder sb = new StringBuilder();
         PythonStreamHandler errorHandler = new PythonStreamHandler(process.getErrorStream(),
         new PythonOutputHandler()
         {
         @Override
         public void outputReceived(String output)
         {
         sb.append(output).append("\n");
         }
         });
         errorHandler.start();

         // Capture r output if an Python output handler is defined
         if (outputHandler != null)
         {
         PythonStreamHandler streamHandler = new PythonStreamHandler(process.getInputStream(), outputHandler);
         streamHandler.start();
         }

         // Wait until script is finished
         process.waitFor();

         // Check for errors
         if (process.exitValue() > 0)
         {
         throw new MolgenisPythonException("Error running [" + script.getAbsolutePath() + "]." + sb.toString());
         }

         LOG.info("Script [" + script.getAbsolutePath() + "] done");
         }
         catch (IOException e)
         {
         throw new MolgenisPythonException("Exception executing PythonScipt.", e);
         }
         catch (InterruptedException e)
         {
         throw new MolgenisPythonException("Exception waiting for PythonScipt to finish", e);
         }
         }
         * */



         //iterate over input again and read from file for SnpEff and annotate Entity

        return null;
    }

    @Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(SNPEFF_EFF, FieldTypeEnum.STRING)); //FIXME: correct type?

		return metadata;
	}

    @Override
    public EntityMetaData getInputMetaData()
    {
        DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
        DefaultAttributeMetaData chrom = new DefaultAttributeMetaData(CHROMOSOME,
                MolgenisFieldTypes.FieldTypeEnum.STRING);
        chrom.setDescription("The chromosome on which the variant is observed");
        DefaultAttributeMetaData pos = new DefaultAttributeMetaData(POSITION, MolgenisFieldTypes.FieldTypeEnum.LONG);
        pos.setDescription("The position on the chromosome which the variant is observed");
        DefaultAttributeMetaData ref = new DefaultAttributeMetaData(REFERENCE, MolgenisFieldTypes.FieldTypeEnum.STRING);
        ref.setDescription("The reference allele");
        DefaultAttributeMetaData alt = new DefaultAttributeMetaData(ALTERNATIVE,
                MolgenisFieldTypes.FieldTypeEnum.STRING);
        alt.setDescription("The alternative allele observed");

        metadata.addAttributeMetaData(chrom);
        metadata.addAttributeMetaData(pos);
        metadata.addAttributeMetaData(ref);
        metadata.addAttributeMetaData(alt);

        return metadata;
    }

    @Override
    public String canAnnotate(EntityMetaData repoMetaData)
    {
        Iterable<AttributeMetaData> annotatorAttributes = getInputMetaData().getAttributes();
        for (AttributeMetaData annotatorAttribute : annotatorAttributes)
        {
            // one of the needed attributes not present? we can not annotate
            if (repoMetaData.getAttribute(annotatorAttribute.getName()) == null)
            {
                return "missing required attribute";
            }

            // one of the needed attributes not of the correct type? we can not annotate
            if (!repoMetaData.getAttribute(annotatorAttribute.getName()).getDataType()
                    .equals(annotatorAttribute.getDataType()))
            {
                return "a required attribute has the wrong datatype";
            }

            // Are the runtime property files not available, or is a webservice down? we can not annotate
            if (!checkSnpEffPath())
            {
                return "SnpEff not found";
            }
        }

        return "true";
    }


}
