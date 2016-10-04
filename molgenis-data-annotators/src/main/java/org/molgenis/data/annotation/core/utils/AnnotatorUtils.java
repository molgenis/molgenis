package org.molgenis.data.annotation.core.utils;

import org.apache.commons.lang.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.core.EffectsAnnotator;
import org.molgenis.data.annotation.core.RefEntityAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.molgenis.data.vcf.utils.VcfUtils;
import org.molgenis.data.vcf.utils.VcfWriterUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MREF;

public class AnnotatorUtils
{
	private static final String EFFECT = "EFFECT";

	public static String getAnnotatorResourceDir()
	{
		// Annotators include files/tools
		String molgenisHomeDir = System.getProperty("molgenis.home");

		if (molgenisHomeDir != null)
		{
			if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';
			return molgenisHomeDir + "data/annotation_resources";
		}
		return null;
	}

	public static Map<String, Double> toAlleleMap(String alternatives, String annotations)
	{
		if (annotations == null) annotations = "";
		if (alternatives == null) return Collections.emptyMap();
		String[] altArray = alternatives.split(",");
		String[] annotationsArray = annotations.split(",");

		Map<String, Double> result = new HashMap<>();
		if (altArray.length == annotationsArray.length)
		{
			for (int i = 0; i < altArray.length; i++)
			{
				Double value = null;
				if (StringUtils.isNotEmpty(annotationsArray[i]))
				{
					value = Double.parseDouble(annotationsArray[i]);
				}
				result.put(altArray[i], value);
			}
		}
		else if (StringUtils.isEmpty(annotations))
		{
			for (String anAltArray : altArray)
			{
				result.put(anAltArray, null);
			}
		}
		else
		{
			throw new MolgenisDataException(VcfAttributes.ALT + " differs in length from the provided annotations.");
		}
		return result;
	}

	/**
	 * Adds a new compound attribute to an existing CrudRepository
	 *
	 * @param entityMetaData           {@link EntityMetaData} for the existing repository
	 * @param attributeFactory
	 * @param annotator
	 */
	public static EntityMetaData addAnnotatorMetaDataToRepositories(EntityMetaData entityMetaData,
			AttributeFactory attributeFactory, RepositoryAnnotator annotator)
	{
		List<Attribute> attributes = annotator.getOutputAttributes();
		Attribute compound;
		String compoundName = annotator.getFullName();
		compound = entityMetaData.getAttribute(compoundName);
		if (compound == null)
		{
			createCompoundForAnnotator(entityMetaData, attributeFactory, annotator, attributes,
					compoundName);
		}
		return entityMetaData;
	}

	private static void createCompoundForAnnotator(EntityMetaData entityMetaData,
			AttributeFactory attributeFactory, RepositoryAnnotator annotator,
			List<Attribute> attributes, String compoundName)
	{
		Attribute compound;
		compound = attributeFactory.create().setName(compoundName).setLabel(annotator.getFullName())
				.setDataType(MolgenisFieldTypes.AttributeType.COMPOUND).setLabel(annotator.getSimpleName());
		Attribute finalCompound = compound;
		attributes.stream().filter(part -> entityMetaData.getAttribute(part.getName()) == null)
				.forEachOrdered(finalCompound::addAttributePart);
		entityMetaData.addAttribute(compound);
	}

	/**
	 * Adds a new compound attribute to an existing CrudRepository
	 *
	 * @param annotator                the annotator to be runned
	 * @param vcfAttributes            utility class for vcf metadata
	 * @param entityMetaDataFactory    factory for molgenis entityMetaData
	 * @param attributeFactory factory for molgenis entityMetaData
	 * @param vcfUtils                 utility class for working with vcf data in molgenis
	 * @param inputVcfFile             the vcf file to be annotated
	 * @param outputVCFFile            the resulting, annotated vcf file
	 * @param attributesToInclude      the attributes of the annotator that should be written to the result
	 * @param update                   boolean indicating if values already present for the annotator attributes should be updated(true) or overwritten (false)
	 * @return the path of the result vcf file
	 * @throws IOException,
	 * @throws MolgenisInvalidFormatException
	 */
	public static String annotate(RepositoryAnnotator annotator, VcfAttributes vcfAttributes,
			EntityMetaDataFactory entityMetaDataFactory, AttributeFactory attributeFactory,
			VcfUtils vcfUtils, File inputVcfFile, File outputVCFFile, List<String> attributesToInclude, boolean update)
			throws IOException, MolgenisInvalidFormatException
	{

		try (BufferedWriter outputVCFWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputVCFFile), UTF_8));
				VcfRepository vcfRepo = new VcfRepository(inputVcfFile, inputVcfFile.getName(), vcfAttributes,
						entityMetaDataFactory, attributeFactory))
		{

			List<Attribute> outputMetaData = getOutputAttributeMetaDatasForAnnotator(annotator,
					entityMetaDataFactory, attributeFactory, attributesToInclude, vcfRepo);

			VcfWriterUtils
					.writeVcfHeader(inputVcfFile, outputVCFWriter, VcfUtils.getAtomicAttributesFromList(outputMetaData),
							attributesToInclude);

			Iterable<Entity> entitiesToAnnotate = addAnnotatorMetaDataToRepository(annotator, attributeFactory,
					vcfUtils, vcfRepo);

			Iterator<Entity> annotatedRecords = annotateRepo(annotator, vcfUtils, update, entitiesToAnnotate);

			writeAnnotationResultToVcfFile(attributesToInclude, outputVCFWriter, outputMetaData, annotatedRecords);
		}
		return outputVCFFile.getAbsolutePath();
	}

	private static Iterator<Entity> annotateRepo(RepositoryAnnotator annotator, VcfUtils vcfUtils, boolean update,
			Iterable<Entity> entitiesToAnnotate)
	{
		Iterator<Entity> annotatedRecords = annotator.annotate(entitiesToAnnotate, update);
		if (annotator instanceof RefEntityAnnotator || annotator instanceof EffectsAnnotator)
		{
			annotatedRecords = vcfUtils.reverseXrefMrefRelation(annotatedRecords);
		}
		return annotatedRecords;
	}

	private static Iterable<Entity> addAnnotatorMetaDataToRepository(RepositoryAnnotator annotator,
			AttributeFactory attributeFactory, VcfUtils vcfUtils, VcfRepository vcfRepo)
	{
		addAnnotatorAttributesToInfoAttribute(annotator, vcfRepo);
		Iterable<Entity> entitiesToAnnotate;

		if (annotator instanceof EffectsAnnotator)
		{
			entitiesToAnnotate = vcfUtils.createEntityStructureForVcf(vcfRepo.getEntityMetaData(), EFFECT,
					StreamSupport.stream(vcfRepo.spliterator(), false));

			// Add metadata to repository that will be annotated, instead of repository with variants
			for (Entity entity : entitiesToAnnotate)
			{
				entity.getEntityMetaData().addAttributes(annotator.getOutputAttributes());
			}
		}
		else
		{
			AnnotatorUtils.addAnnotatorMetaDataToRepositories(vcfRepo.getEntityMetaData(), attributeFactory,
					annotator);
			entitiesToAnnotate = vcfRepo;
		}
		return entitiesToAnnotate;
	}

	private static void writeAnnotationResultToVcfFile(List<String> attributesToInclude, BufferedWriter outputVCFWriter,
			List<Attribute> outputMetaData, Iterator<Entity> annotatedRecords) throws IOException
	{
		while (annotatedRecords.hasNext())
		{
			// annotation starts here
			Entity annotatedRecord = annotatedRecords.next();
			VcfWriterUtils.writeToVcf(annotatedRecord, VcfUtils.getAtomicAttributesFromList(outputMetaData),
					attributesToInclude, outputVCFWriter);
			outputVCFWriter.newLine();
		}
	}

	private static void addAnnotatorAttributesToInfoAttribute(RepositoryAnnotator annotator, VcfRepository vcfRepo)
	{
		EntityMetaData emd = vcfRepo.getEntityMetaData();
		Attribute infoAttribute = emd.getAttribute(VcfAttributes.INFO);
		for (Attribute attribute : annotator.getOutputAttributes())
		{
			for (Attribute atomicAttribute : attribute.getAttributeParts())
			{
				infoAttribute.addAttributePart(atomicAttribute);
			}
		}
	}

	private static List<Attribute> getOutputAttributeMetaDatasForAnnotator(RepositoryAnnotator annotator,
			EntityMetaDataFactory entityMetaDataFactory, AttributeFactory attributeFactory,
			List<String> attributesToInclude, VcfRepository vcfRepo)
	{
		if (!attributesToInclude.isEmpty())
		{
			checkSelectedOutputAttributeNames(annotator, attributesToInclude, vcfRepo);
		}
		// If the annotator e.g. SnpEff creates an external repository, collect the output metadata into an mref
		// entity
		// This allows for the header to be written as 'EFFECT annotations: <ouput_attributes> | <ouput_attributes>'
		List<Attribute> outputMetaData = newArrayList();
		if (annotator instanceof RefEntityAnnotator || annotator instanceof EffectsAnnotator)
		{
			EntityMetaData effectRefEntity = entityMetaDataFactory.create()
					.setName(annotator.getSimpleName() + "_EFFECTS");
			for (Attribute outputAttribute : annotator.getOutputAttributes())
			{
				effectRefEntity.addAttribute(outputAttribute);
			}
			Attribute effect = attributeFactory.create().setName(EFFECT);
			effect.setDataType(MREF).setRefEntity(effectRefEntity);
			outputMetaData.add(effect);
		}
		else
		{
			outputMetaData = annotator.getOutputAttributes();
		}
		return outputMetaData;
	}

	private static void checkSelectedOutputAttributeNames(RepositoryAnnotator annotator,
			List<String> attributesToInclude, VcfRepository vcfRepo)
	{
		// Check attribute names
		List<String> outputAttributeNames = VcfUtils.getAtomicAttributesFromList(annotator.getOutputAttributes())
				.stream().map(Attribute::getName).collect(Collectors.toList());

		List<String> inputAttributeNames = VcfUtils
				.getAtomicAttributesFromList(vcfRepo.getEntityMetaData().getAtomicAttributes()).stream()
				.map(Attribute::getName).collect(Collectors.toList());

		for (Object attrName : attributesToInclude)
		{
			if (!outputAttributeNames.contains(attrName))
			{
				throw new RuntimeException("Unknown output attribute '" + attrName + "'");
			}
			else if (inputAttributeNames.contains(attrName))
			{
				throw new RuntimeException("The output attribute '" + attrName
						+ "' is present in the inputfile, but is deselected in the current run, this is not supported");
			}
		}
	}
}
