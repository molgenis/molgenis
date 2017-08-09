package org.molgenis.data.annotation.core.entity;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;

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
	/**
	 * @return {@link AnnotatorInfo} for the {@link Annotator}
	 */
	AnnotatorInfo getInfo();

	/**
	 * Be aware that several calls to this function will give you the same auto identifiers every time
	 *
	 * @return List of {@link Attribute} for the attributes that get added by this {@link Annotator}
	 */
	List<Attribute> getAnnotatorAttributes();

	/**
	 * Use this method for the actual annotation process, to ensure that unique
	 * identifiers by creating a new set of attributes everytime an annotator us executed
	 *
	 * @param attributeFactory
	 * @return
	 */
	List<Attribute> createAnnotatorAttributes(AttributeFactory attributeFactory);
}
