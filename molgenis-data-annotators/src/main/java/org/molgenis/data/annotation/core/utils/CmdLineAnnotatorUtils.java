package org.molgenis.data.annotation.core.utils;

import net.sf.samtools.util.BlockCompressedOutputStream;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.annotation.core.EffectBasedAnnotator;
import org.molgenis.data.annotation.core.EffectCreatingAnnotator;
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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.data.meta.AttributeType.MREF;

public class CmdLineAnnotatorUtils
{
	private static final String EFFECT = "EFFECT";

	/**
	 * Adds a new compound attribute to an existing CrudRepository
	 *
	 * @param annotator                the annotator to be runned
	 * @param vcfAttributes            utility class for vcf metadata
	 * @param entityTypeFactory        factory for molgenis entityType
	 * @param attributeFactory         factory for molgenis entityType
	 * @param effectStructureConverter utility class for converting a vcfRepo from and to the molgenis entity structure for "effects" annotations
	 * @param inputVcfFile             the vcf file to be annotated
	 * @param outputVCFFile            the resulting, annotated vcf file
	 * @param attributesToInclude      the attributes of the annotator that should be written to the result
	 * @param update                   boolean indicating if values already present for the annotator attributes should be updated(true) or overwritten (false)
	 * @return the path of the result vcf file
	 * @throws IOException,
	 * @throws MolgenisInvalidFormatException
	 */
	public static String annotate(RepositoryAnnotator annotator, VcfAttributes vcfAttributes,
			EntityTypeFactory entityTypeFactory, AttributeFactory attributeFactory,
			EffectStructureConverter effectStructureConverter, File inputVcfFile, File outputVCFFile,
			List<String> attributesToInclude, boolean update) throws IOException, MolgenisInvalidFormatException
	{

		try (BufferedWriter outputVCFWriter = createBufferedWriter(outputVCFFile);
				VcfRepository vcfRepo = new VcfRepository(inputVcfFile, inputVcfFile.getName(), vcfAttributes,
						entityTypeFactory, attributeFactory))
		{

			List<Attribute> outputMetaData = getOutputAttributeMetadatasForAnnotator(annotator, entityTypeFactory,
					attributeFactory, attributesToInclude, vcfRepo);

			VcfWriterUtils.writeVcfHeader(inputVcfFile, outputVCFWriter,
					VcfUtils.getAtomicAttributesFromList(outputMetaData), attributesToInclude);

			Iterable<Entity> entitiesToAnnotate = addAnnotatorMetaDataToRepository(annotator, attributeFactory,
					effectStructureConverter, vcfRepo);

			Iterator<Entity> annotatedRecords = annotateRepo(annotator, effectStructureConverter, update,
					entitiesToAnnotate);

			writeAnnotationResultToVcfFile(attributesToInclude, outputVCFWriter, outputMetaData, annotatedRecords);
		}
		return outputVCFFile.getAbsolutePath();
	}

	private static BufferedWriter createBufferedWriter(File outputVCFFile) throws IOException
	{
		OutputStream outputStream;
		if (outputVCFFile.getName().endsWith(".gz"))
		{
			outputStream = new BlockCompressedOutputStream(outputVCFFile);
		}
		else
		{
			outputStream = new FileOutputStream(outputVCFFile);
		}
		return new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8));
	}

	private static Iterator<Entity> annotateRepo(RepositoryAnnotator annotator,
			EffectStructureConverter effectStructureConverter, boolean update, Iterable<Entity> entitiesToAnnotate)
	{
		Iterator<Entity> annotatedRecords = annotator.annotate(entitiesToAnnotate, update);
		if (annotator instanceof EffectCreatingAnnotator || annotator instanceof EffectBasedAnnotator)
		{
			annotatedRecords = effectStructureConverter.createVcfEntityStructure(annotatedRecords);
		}
		return annotatedRecords;
	}

	private static Iterable<Entity> addAnnotatorMetaDataToRepository(RepositoryAnnotator annotator,
			AttributeFactory attributeFactory, EffectStructureConverter effectStructureConverter, VcfRepository vcfRepo)
	{
		addAnnotatorAttributesToInfoAttribute(annotator, vcfRepo);
		Stream<Entity> entitiesToAnnotate;

		// Check if annotator is annotator that annotates effects (for example Gavin)
		if (annotator instanceof EffectBasedAnnotator)
		{
			entitiesToAnnotate = effectStructureConverter.createVariantEffectStructure(EFFECT,
					annotator.getOutputAttributes(), vcfRepo);
		}
		else
		{
			AnnotatorUtils.addAnnotatorMetaDataToRepositories(vcfRepo.getEntityType(), attributeFactory, annotator);
			return vcfRepo;
		}

		return entitiesToAnnotate::iterator;
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
		if (annotator instanceof EffectCreatingAnnotator || annotator instanceof EffectBasedAnnotator)
		{
			EntityType effectRefEntity = entityTypeFactory.create(annotator.getSimpleName() + "_EFFECTS");
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
													.stream()
													.map(Attribute::getName)
													.collect(Collectors.toList());

		List<String> inputAttributeNames = VcfUtils.getAtomicAttributesFromList(
				vcfRepo.getEntityType().getAtomicAttributes())
												   .stream()
												   .map(Attribute::getName)
												   .collect(Collectors.toList());

		for (String attrName : attributesToInclude)
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
