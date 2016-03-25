package org.molgenis.data.annotation;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;

import java.util.LinkedList;

public abstract class AbstractExternalRepositoryAnnotator extends AbstractRepositoryAnnotator
{
	public abstract EntityMetaData getOutputMetaData(EntityMetaData sourceEMD);

	public abstract LinkedList<AttributeMetaData> getOrderedAttributeList(EntityMetaData sourceEMD);
}
