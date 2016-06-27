package org.molgenis.data.annotation.entity;

import java.util.List;

import org.molgenis.data.meta.model.AttributeMetaData;

/**
 * An Annotator.
 * 
 * Annotators enrich source data with extra attributes.
 * 
 * @author fkelpin
 */
public interface Annotator
{
	public static final String ANNOTATORPREFIX = "MOLGENIS_";
	/**
	 * @return {@link AnnotatorInfo} for the {@link Annotator}
	 */
	AnnotatorInfo getInfo();

	/**
	 * @return Compound {@link AttributeMetaData} for the attributes that get added by this {@link Annotator}
	 */
	List<AttributeMetaData> getAnnotationAttributeMetaDatas();
}
