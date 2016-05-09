package org.molgenis.data.annotation.meta;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Package;
import org.molgenis.data.support.DefaultEntityMetaData;

import java.util.LinkedList;

public interface AnnotatorEntityMetaData{
    LinkedList<AttributeMetaData> getOrderedAttributes();
}
