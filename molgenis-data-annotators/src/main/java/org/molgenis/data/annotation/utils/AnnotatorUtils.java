package org.molgenis.data.annotation.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;

import autovalue.shaded.com.google.common.common.collect.Iterables;
import org.molgenis.data.vcf.VcfRepository;

public class AnnotatorUtils
{
	public static DefaultAttributeMetaData getCompoundResultAttribute(RepositoryAnnotator annotator,
			EntityMetaData entityMetaData)
	{
		DefaultAttributeMetaData compoundAttributeMetaData = new DefaultAttributeMetaData(annotator.getFullName(),
				MolgenisFieldTypes.FieldTypeEnum.COMPOUND);
		compoundAttributeMetaData.setLabel(annotator.getSimpleName());

		List<AttributeMetaData> outputAttrs = annotator.getOutputMetaData();

		if (outputAttrs.size() == 1
				&& Iterables.get(outputAttrs, 0).getDataType().getEnumType()
						.equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
		{
			compoundAttributeMetaData = (DefaultAttributeMetaData) outputAttrs.get(0);
		}
		else
		{
			for (AttributeMetaData currentAmd : outputAttrs)
			{
				String currentAttributeName = currentAmd.getName();
				if (entityMetaData.getAttribute(currentAttributeName) == null)
				{
					compoundAttributeMetaData.addAttributePart(currentAmd);
				}
			}
		}
		return compoundAttributeMetaData;
	}

	public static String getAnnotatorResourceDir()
	{
		// Annotators include files/tools
		String molgenisHomeDir = System.getProperty("molgenis.home");

		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}

		if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';
		return molgenisHomeDir + "data/annotation_resources";
	}

	public static Map<String, Double> toAlleleMap(String alternatives, String annotations)
	{
		if (annotations == null) annotations = "";
		if(alternatives==null) return Collections.emptyMap();
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
			throw new MolgenisDataException(VcfRepository.ALT + " differs in length from the provided annotations.");
		}
		return result;
	}
}
