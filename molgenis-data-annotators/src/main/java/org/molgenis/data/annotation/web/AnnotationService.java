package org.molgenis.data.annotation.web;

import org.molgenis.data.annotation.core.RepositoryAnnotator;
import org.molgenis.data.meta.model.EntityType;

import java.util.List;

/**
 * Created with IntelliJ IDEA. User: charbonb Date: 19/02/14 Time: 12:50 To change this template use File | Settings |
 * File Templates.
 */
public interface AnnotationService
{

	RepositoryAnnotator getAnnotatorByName(String annotatorName);

	List<RepositoryAnnotator> getAllAnnotators();

	List<RepositoryAnnotator> getAnnotatorsByMetaData(EntityType metaData);

}
