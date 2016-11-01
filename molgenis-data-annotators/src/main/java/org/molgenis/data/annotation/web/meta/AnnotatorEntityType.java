package org.molgenis.data.annotation.web.meta;

import org.molgenis.data.meta.model.Attribute;

import java.util.LinkedList;

public interface AnnotatorEntityType
{
	LinkedList<Attribute> getOrderedAttributes();
}
