package org.molgenis.data.annotation.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.COMPOUND;

public class AnnotatorUtils
{
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
		List<Attribute> attributes = annotator.createAnnotatorAttributes(attributeFactory);
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
		compound = attributeFactory.create()
								   .setName(compoundName)
								   .setLabel(annotator.getFullName())
								   .setDataType(COMPOUND)
								   .setLabel(annotator.getSimpleName());

		attributes.stream()
				  .filter(part -> entityType.getAttribute(part.getName()) == null)
				  .forEachOrdered(part -> part.setParent(compound));

		entityType.addAttribute(compound);

		// Only add attributes that do not already exist. We assume existing attributes are added in a previous annotation run.
		// This is a potential risk if an attribute with that name already exist that was not added by the annotator.
		// This risk is relatively low since annotator attributes are prefixed.
		attributes = attributes.stream()
							   .filter(attribute -> entityType.getAttribute(attribute.getName()) == null)
							   .collect(toList());

		entityType.addAttributes(attributes);
	}
}
