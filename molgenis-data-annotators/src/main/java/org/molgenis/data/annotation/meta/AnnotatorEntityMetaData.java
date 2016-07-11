package org.molgenis.data.annotation.meta;


import org.molgenis.data.meta.model.AttributeMetaData;

import java.util.LinkedList;

public interface AnnotatorEntityMetaData{
    LinkedList<AttributeMetaData> getOrderedAttributes();
}
