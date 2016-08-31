package org.molgenis.data.annotation.core.utils;

import org.apache.commons.lang.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.core.EffectsAnnotator;
import org.molgenis.data.annotation.core.RefEntityAnnotator;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
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
			for (int i = 0; i < altArray.length; i++)
			{
				result.put(altArray[i], null);
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
	 * @param attributeMetaDataFactory
	 * @param annotator
	 */
	public static EntityMetaData addAnnotatorMetadataToRepositories(EntityMetaData entityMetaData,
			AttributeMetaDataFactory attributeMetaDataFactory, RepositoryAnnotator annotator)
	{
		List<AttributeMetaData> attributeMetaDatas = annotator.getOutputAttributes();
		AttributeMetaData compound;
		String compoundName = annotator.getFullName();
		compound = entityMetaData.getAttribute(compoundName);
		if (compound == null)
		{
			compound = attributeMetaDataFactory.create().setName(compoundName).setLabel(annotator.getFullName())
					.setDataType(MolgenisFieldTypes.AttributeType.COMPOUND).setLabel(annotator.getSimpleName());
			AttributeMetaData finalCompound = compound;
			attributeMetaDatas.stream().filter(part -> entityMetaData.getAttribute(part.getName()) == null)
					.forEachOrdered(part -> finalCompound.addAttributePart(part));
			entityMetaData.addAttribute(compound);
		}
		return entityMetaData;
	}

	public static String annotate(RepositoryAnnotator annotator, VcfAttributes vcfAttributes,
			EntityMetaDataFactory entityMetaDataFactory, AttributeMetaDataFactory attributeMetaDataFactory,
			VcfUtils vcfUtils, File inputVcfFile, File outputVCFFile, List<String> attributesToInclude, boolean update)
			throws IOException, MolgenisInvalidFormatException
	{

		try (BufferedWriter outputVCFWriter = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outputVCFFile), UTF_8));
				VcfRepository vcfRepo = new VcfRepository(inputVcfFile, inputVcfFile.getName(), vcfAttributes,
						entityMetaDataFactory, attributeMetaDataFactory))
		{
			if (!attributesToInclude.isEmpty())
			{
				// Check attribute names
				List<String> outputAttributeNames = VcfUtils
						.getAtomicAttributesFromList(annotator.getOutputAttributes()).stream()
						.map(AttributeMetaData::getName).collect(Collectors.toList());

				List<String> inputAttributeNames = VcfUtils
						.getAtomicAttributesFromList(vcfRepo.getEntityMetaData().getAtomicAttributes()).stream()
						.map(AttributeMetaData::getName).collect(Collectors.toList());

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

			// If the annotator e.g. SnpEff creates an external repository, collect the output metadata into an mref
			// entity
			// This allows for the header to be written as 'EFFECT annotations: <ouput_attributes> | <ouput_attributes>'
			List<AttributeMetaData> outputMetaData = newArrayList();
			if (annotator instanceof RefEntityAnnotator || annotator instanceof EffectsAnnotator)
			{
				EntityMetaData effectRefEntity = entityMetaDataFactory.create()
						.setName(annotator.getSimpleName() + "_EFFECTS");
				for (AttributeMetaData outputAttribute : annotator.getOutputAttributes())
				{
					effectRefEntity.addAttribute(outputAttribute);
				}
				AttributeMetaData effect = attributeMetaDataFactory.create().setName(EFFECT);
				effect.setDataType(MREF).setRefEntity(effectRefEntity);
				outputMetaData.add(effect);
			}
			else
			{
				outputMetaData = annotator.getOutputAttributes();
			}

			VcfWriterUtils
					.writeVcfHeader(inputVcfFile, outputVCFWriter, VcfUtils.getAtomicAttributesFromList(outputMetaData),
							attributesToInclude);
			System.out.println("Now starting to process the data.");

			EntityMetaData emd = vcfRepo.getEntityMetaData();
			AttributeMetaData infoAttribute = emd.getAttribute(VcfAttributes.INFO);
			for (AttributeMetaData attribute : annotator.getOutputAttributes())
			{
				for (AttributeMetaData atomicAttribute : attribute.getAttributeParts())
				{
					infoAttribute.addAttributePart(atomicAttribute);
				}
			}
			Iterable<Entity> entitiesToAnnotate;

			AnnotatorUtils.addAnnotatorMetadataToRepositories(vcfRepo.getEntityMetaData(), attributeMetaDataFactory,
					annotator);
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
				entitiesToAnnotate = vcfRepo;
			}

			// these methods wrap iterators around the entities to make it streaming, actual annotation starts later
			Iterator<Entity> annotatedRecords = annotator.annotate(entitiesToAnnotate, update);
			if (annotator instanceof RefEntityAnnotator || annotator instanceof EffectsAnnotator)
			{
				annotatedRecords = vcfUtils.reverseXrefMrefRelation(annotatedRecords);
			}

			while (annotatedRecords.hasNext())
			{
				// annotation starts here
				Entity annotatedRecord = annotatedRecords.next();
				VcfWriterUtils.writeToVcf(annotatedRecord, VcfUtils.getAtomicAttributesFromList(outputMetaData),
						attributesToInclude, outputVCFWriter);
				outputVCFWriter.newLine();
			}

		}
		return outputVCFFile.getAbsolutePath();
	}
}
