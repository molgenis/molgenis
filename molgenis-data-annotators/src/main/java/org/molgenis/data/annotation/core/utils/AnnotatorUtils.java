package org.molgenis.data.annotation.core.utils;

import org.apache.commons.lang.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.vcf.model.VcfAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotatorUtils
{
	public static final String MOLGENIS_PREFIX = "MOLGENIS_";

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
}
