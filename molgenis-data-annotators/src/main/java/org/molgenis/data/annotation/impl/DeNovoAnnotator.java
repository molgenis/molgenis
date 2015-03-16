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

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.annotation.VcfUtils;
import org.molgenis.data.annotation.impl.datastructures.Sample;
import org.molgenis.data.annotation.impl.datastructures.Trio;
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
 * De novo variant annotator
 * Uses trio data (child-mother-father), read from VCF pedigree data
 * See: http://samtools.github.io/hts-specs/VCFv4.2.pdf
 * 
 *
 **/
@Component("deNovoService")
public class DeNovoAnnotator extends VariantAnnotator
{
	private static final Logger LOG = LoggerFactory.getLogger(DeNovoAnnotator.class);

	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;
	private HashMap<String, Trio> pedigree;
	
	//helper lists for ease of use, derived from HashMap<String, Parents> pedigree
	private HashMap<String, Sample> motherToChild;
	private HashMap<String, Sample> fatherToChild;

	public static final String DENOVO = VcfRepository.getInfoPrefix() + "DENOVO";

	private static final String NAME = "DENOVO";

	final List<String> infoFields = Arrays
			.asList(new String[]
			{
					"##INFO=<ID="
							+ DENOVO.substring(VcfRepository.getInfoPrefix().length())
							+ ",Number=1,Type=String,Description=\"todo\">"
							});

	@Autowired
	public DeNovoAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
			throws IOException
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	public DeNovoAnnotator(File exacFileLocation, File inputVcfFile, File outputVCFFile) throws Exception
	{

		this.molgenisSettings = new MolgenisSimpleSettings();
		this.annotatorService = new AnnotationServiceImpl();
		
		PrintWriter outputVCFWriter = new PrintWriter(outputVCFFile, "UTF-8");

		VcfRepository vcfRepo = new VcfRepository(inputVcfFile, this.getClass().getName());
		Iterator<Entity> vcfIter = vcfRepo.iterator();

		VcfUtils.checkPreviouslyAnnotatedAndAddMetadata(inputVcfFile, outputVCFWriter, infoFields, DENOVO.substring(VcfRepository.getInfoPrefix().length()));

		pedigree = VcfUtils.getPedigree(inputVcfFile);
		motherToChild = new HashMap<String, Sample>();
		fatherToChild =  new HashMap<String, Sample>();
		for(String key: pedigree.keySet())
		{
			Sample mom = pedigree.get(key).getMother();
			Sample dad = pedigree.get(key).getFather();
			motherToChild.put(mom.getId(), pedigree.get(key).getChild());
			fatherToChild.put(dad.getId(), pedigree.get(key).getChild());
		}
		
		for(String key : pedigree.keySet())
		{
			System.out.println(key+ ", " + pedigree.get(key));
		}
		
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
		return true; //no annotation data required ?
	}

	@Override
	public String getSimpleName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		Map<String, Object> resultMap = annotateEntityWithDeNovo(entity);
		return Collections.<Entity> singletonList(getAnnotatedEntity(entity, resultMap));
	}

	

	private synchronized Map<String, Object> annotateEntityWithDeNovo(Entity entity) throws IOException
	{
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		Iterable<Entity> samples = entity.getEntities("Samples");
		
		HashMap<String, Trio> childToParentGenotypes = new HashMap<String, Trio>();
		
		System.out.println("Variant: " + entity.get(VcfRepository.CHROM) + " " + entity.get(VcfRepository.POS) + " " + entity.get(VcfRepository.REF) + " " + entity.get(VcfRepository.ALT));
		for(Entity sample : samples)
		{
			String sampleID = sample.get("NAME").toString().substring(sample.get("NAME").toString().lastIndexOf("_")+1);
			String gt = sample.get("GT") == null ? null : sample.get("GT").toString();
			
	//		System.out.println("SAMPLEID: " + sampleID);
	//		System.out.println("GT: " + gt);
			
			//found a child
			if(pedigree.keySet().contains(sampleID))
			{
				//child not previously added by finding a parent first, add here plus empty trio object
				if(!childToParentGenotypes.containsKey(sampleID))
				{
//					System.out.println("CHILD - NEW TRIO");
						Trio t = new Trio();
						t.setChild(new Sample(sampleID, gt));
						childToParentGenotypes.put(sampleID, t);
				}
				//child may have been added because the parent was found first, in that case there is only a key, so make child object + genotype!
				else if(childToParentGenotypes.containsKey(sampleID) && gt != null)
				{
//					System.out.println("CHILD - UPDATING GENOTYPE");
					if(childToParentGenotypes.get(sampleID)
							.getChild()
							!= null)
					{
						throw new IOException("Child genotype for '"+sampleID+"' already known !");
					}
					Sample child = new Sample(sampleID, gt);
					childToParentGenotypes.get(sampleID).setChild(child);
				}
			}
			
			//found a mother
			else if(motherToChild.containsKey(sampleID))
			{
				//child not seen yet, look it up and add it here, but without genotype (!)
				if(!childToParentGenotypes.containsKey(motherToChild.get(sampleID).getId()))
				{
//					System.out.println("MOTHER - NEW TRIO");
					Trio t = new Trio();
					t.setMother(new Sample(sampleID, gt));
					childToParentGenotypes.put(motherToChild.get(sampleID).getId(), t);
				}
				//child seen, check if mother was already known, we do not expect/want this to happen
				else
				{
					if(childToParentGenotypes.get(motherToChild.get(sampleID).getId()).getMother() != null)
					{
						throw new IOException("Mother '"+sampleID+"' already known for child '" + motherToChild.get(sampleID) + "' !");
					}
//					System.out.println("MOTHER - UPDATING GENOTYPE");
					childToParentGenotypes.get(motherToChild.get(sampleID).getId()).setMother(new Sample(sampleID, gt));
				}
			}
			else if(fatherToChild.containsKey(sampleID))
			{
				//child not seen yet, look it up and add it here, but without genotype (!)
				if(!childToParentGenotypes.containsKey(fatherToChild.get(sampleID).getId()))
				{
//					System.out.println("FATHER - NEW TRIO");
					Trio t = new Trio();
					t.setFather(new Sample(sampleID, gt));
					childToParentGenotypes.put(fatherToChild.get(sampleID).getId(), t);
				}
				//child seen, check if father was already known, we do not expect/want this to happen
				else
				{
					if(childToParentGenotypes.get(fatherToChild.get(sampleID).getId()).getFather() != null)
					{
						throw new IOException("Father '"+sampleID+"' already known for child '" + fatherToChild.get(sampleID) + "' !");
					}	
//					System.out.println("FATHER - UPDATING GENOTYPE");
					childToParentGenotypes.get(fatherToChild.get(sampleID).getId()).setFather(new Sample(sampleID, gt));
				}
			}
			else
			{
				LOG.warn("Sample ID '" + sampleID + "' not in list of children, mothers or fathers !! ignoring for further analysis !!");
			}
			
	//		System.out.println("sampleID " + sampleID);
	//		System.out.println("\t" + sample.get("GT") + " is a " + (pedigree.containsKey(sampleID) ? "child" : "parent"));
		}

		/**
		System.out.println("Found " + childToParentGenotypes.size() + " trios..");
		for(String child : childToParentGenotypes.keySet())
		{
			System.out.println(child + ", " + childToParentGenotypes.get(child));
		}
		*/
		
		
		
		resultMap.put(DENOVO, "yes");
		return resultMap;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(DENOVO, FieldTypeEnum.STRING));
		return metadata;
	}

}
