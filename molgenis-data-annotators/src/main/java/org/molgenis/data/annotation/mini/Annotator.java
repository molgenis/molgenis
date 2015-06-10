package org.molgenis.data.annotation.mini;

import org.molgenis.data.AttributeMetaData;

/**
 * An Annotator.
 * 
 * Annotators enrich source data with extra attributes.
 * 
 * @author fkelpin
 */
public interface Annotator
{
	/**
	 * @return {@link AnnotatorInfo} for the {@link Annotator}
	 */
	AnnotatorInfo getInfo();

	/**
	 * @return Compound {@link AttributeMetaData} for the attributes that get added by this {@link Annotator}
	 */
	AttributeMetaData getAnnotationAttributeMetaData();
}
