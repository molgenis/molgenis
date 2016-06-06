package org.molgenis.data.annotation.utils;

import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.AttributeMetaDataFactory;
import org.molgenis.data.meta.EntityMetaData;

import autovalue.shaded.com.google.common.common.collect.Iterables;

public class AnnotatorUtils
{
	public static AttributeMetaData getCompoundResultAttribute(RepositoryAnnotator annotator,
			EntityMetaData entityMetaData)
	{
		AttributeMetaDataFactory attrMetaFactory = getApplicationContext().getBean(AttributeMetaDataFactory.class);
		AttributeMetaData compoundAttributeMetaData = attrMetaFactory.create().setName(annotator.getFullName())
				.setDataType(COMPOUND);
		compoundAttributeMetaData.setLabel(annotator.getSimpleName());

		List<AttributeMetaData> outputAttrs = annotator.getOutputMetaData();

		if (outputAttrs.size() == 1
				&& Iterables.get(outputAttrs, 0).getDataType().getEnumType()
						.equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
		{
			compoundAttributeMetaData = outputAttrs.get(0);
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
}
