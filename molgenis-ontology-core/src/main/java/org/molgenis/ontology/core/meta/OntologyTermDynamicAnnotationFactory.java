package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermDynamicAnnotationFactory extends
		AbstractSystemEntityFactory<OntologyTermDynamicAnnotation, OntologyTermDynamicAnnotationMetaData, String>
{
	@Autowired
	OntologyTermDynamicAnnotationFactory(OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMeta)
	{
		super(OntologyTermDynamicAnnotation.class, ontologyTermDynamicAnnotationMeta);
	}
}
