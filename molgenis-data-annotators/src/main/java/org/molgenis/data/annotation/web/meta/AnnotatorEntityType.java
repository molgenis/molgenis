package org.molgenis.data.annotation.web.meta;

import org.molgenis.data.meta.model.AttributeMetaData;

import java.util.LinkedList;

public interface AnnotatorEntityType
{
	LinkedList<AttributeMetaData> getOrderedAttributes();
}
