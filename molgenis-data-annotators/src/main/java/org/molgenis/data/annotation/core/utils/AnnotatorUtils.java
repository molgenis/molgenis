package org.molgenis.data.annotation.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.core.EffectsAnnotator;
import org.molgenis.data.annotation.core.RefEntityAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
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
import static org.molgenis.AttributeType.COMPOUND;
import static org.molgenis.AttributeType.MREF;

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
	 * @param entityType       {@link EntityType} for the existing repository
	 * @param attributeFactory
	 * @param annotator
	 */
	public static EntityType addAnnotatorMetaDataToRepositories(EntityType entityType,
			AttributeFactory attributeFactory, RepositoryAnnotator annotator)
	{
		List<Attribute> attributes = annotator.getOutputAttributes();
		Attribute compound;
		String compoundName = annotator.getFullName();
		compound = entityType.getAttribute(compoundName);
		if (compound == null)
		{
			createCompoundForAnnotator(entityType, attributeFactory, annotator, attributes, compoundName);
		}
		return entityType;
	}

	private static void createCompoundForAnnotator(EntityType entityType, AttributeFactory attributeFactory,
			RepositoryAnnotator annotator, List<Attribute> attributes, String compoundName)
	{
		Attribute compound;
		compound = attributeFactory.create().setName(compoundName).setLabel(annotator.getFullName())
				.setDataType(COMPOUND).setLabel(annotator.getSimpleName());
		attributes.stream().filter(part -> entityType.getAttribute(part.getName()) == null)
				.forEachOrdered(part -> part.setParent(compound));
		entityType.addAttribute(compound);
		entityType.addAttributes(attributes);
	}

	/**
	 * Adds a new compound attribute to an existing CrudRepository
	 *
	 * @param annotator           the annotator to be runned
	 * @param vcfAttributes       utility class for vcf metadata
	 * @param entityTypeFactory   factory for molgenis entityType
	 * @param attributeFactory    factory for molgenis entityType
	 * @param vcfUtils            utility class for working with vcf data in molgenis
	 * @param inputVcfFile        the vcf file to be annotated
	 * @param outputVCFFile       the resulting, annotated vcf file
	 * @param attributesToInclude the attributes of the annotator that should be written to the result
	 * @param update              boolean indicating if values already present for the annotator attributes should be updated(true) or overwritten (false)
	 * @return the path of the result vcf file
	 * @throws IOException,
	 * @throws MolgenisInvalidFormatException
	 */
	public static String annotate(RepositoryAnnotator annotator, VcfAttributes vcfAttributes,
			EntityTypeFactory entityTypeFactory, AttributeFactory attributeFactory, VcfUtils vcfUtils,
			File inputVcfFile, File outputVCFFile, List<String> attributesToInclude, boolean update)
			throws IOException, MolgenisInvalidFormatException
	{

		try (BufferedWriter outputVCFWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputVCFFile), UTF_8));
				VcfRepository vcfRepo = new VcfRepository(inputVcfFile, inputVcfFile.getName(), vcfAttributes,
						entityTypeFactory, attributeFactory))
		{

			List<Attribute> outputMetaData = getOutputAttributeMetadatasForAnnotator(annotator, entityTypeFactory,
					attributeFactory, attributesToInclude, vcfRepo);

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
			entitiesToAnnotate = vcfUtils.createEntityStructureForVcf(vcfRepo.getEntityType(), EFFECT,
					StreamSupport.stream(vcfRepo.spliterator(), false));

			// Add metadata to repository that will be annotated, instead of repository with variants
			for (Entity entity : entitiesToAnnotate)
			{
				entity.getEntityType().addAttributes(annotator.getOutputAttributes());
			}
		}
		else
		{
			AnnotatorUtils.addAnnotatorMetaDataToRepositories(vcfRepo.getEntityType(), attributeFactory, annotator);
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
		EntityType entityType = vcfRepo.getEntityType();
		Attribute infoAttribute = entityType.getAttribute(VcfAttributes.INFO);
		for (Attribute attribute : annotator.getOutputAttributes())
		{
			for (Attribute atomicAttribute : attribute.getChildren())
			{
				atomicAttribute.setParent(infoAttribute);
				entityType.addAttribute(atomicAttribute);
			}
		}
	}

	private static List<Attribute> getOutputAttributeMetadatasForAnnotator(RepositoryAnnotator annotator,
			EntityTypeFactory entityTypeFactory, AttributeFactory attributeFactory, List<String> attributesToInclude,
			VcfRepository vcfRepo)
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
			EntityType effectRefEntity = entityTypeFactory.create().setName(annotator.getSimpleName() + "_EFFECTS");
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
				.getAtomicAttributesFromList(vcfRepo.getEntityType().getAtomicAttributes()).stream()
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
