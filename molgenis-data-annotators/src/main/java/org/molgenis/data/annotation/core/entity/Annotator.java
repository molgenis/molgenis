package org.molgenis.data.annotation.core.entity;

import org.molgenis.data.meta.model.Attribute;

import java.util.List;

/**
 * An Annotator.
 * <p>
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
	 * @return Compound {@link Attribute} for the attributes that get added by this {@link Annotator}
	 */
	List<Attribute> getAnnotationAttributeMetaDatas();
}
