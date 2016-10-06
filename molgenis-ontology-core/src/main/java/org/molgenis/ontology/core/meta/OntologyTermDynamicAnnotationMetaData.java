package org.molgenis.ontology.core.meta;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.meta.OntologyPackage.PACKAGE_ONTOLOGY;

@Component
public class OntologyTermDynamicAnnotationMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "OntologyTermDynamicAnnotation";
	public final static String ONTOLOGY_TERM_DYNAMIC_ANNOTATION = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ID = "id";
	public final static String NAME = "name";
	public final static String VALUE = "value";
	public final static String LABEL = "label";

	private final OntologyPackage ontologyPackage;

	@Autowired
	public OntologyTermDynamicAnnotationMetaData(OntologyPackage ontologyPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = requireNonNull(ontologyPackage);
	}

	@Override
	public void init()
	{
		setLabel("Ontology term dynamic annotation");
		setPackage(ontologyPackage);

		addAttribute(ID, ROLE_ID).setVisible(false);
		addAttribute(NAME).setNillable(false);
		addAttribute(VALUE).setNillable(false);
		addAttribute(LABEL, ROLE_LABEL).setNillable(false);
	}
}