package org.molgenis.ontology.sorta.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.core.model.OntologyPackage;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.*;
import static org.molgenis.ontology.core.model.OntologyPackage.PACKAGE_ONTOLOGY;

@Component
public class OntologyTermHitMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "OntologyTermHit";
	public static final String ONTOLOGY_TERM_HIT = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String SCORE = "Score";
	public static final String COMBINED_SCORE = "Combined_Score";

	private final OntologyPackage ontologyPackage;
	private final OntologyTermSynonymMetaData ontologyTermSynonymMetaData;
	private final OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMetaData;
	private final OntologyTermNodePathMetaData ontologyTermNodePathMetaData;
	private final OntologyMetaData ontologyMetaData;

	public OntologyTermHitMetaData(OntologyPackage ontologyPackage,
			OntologyTermSynonymMetaData ontologyTermSynonymMetaData,
			OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMetaData,
			OntologyTermNodePathMetaData ontologyTermNodePathMetaData, OntologyMetaData ontologyMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = requireNonNull(ontologyPackage);
		this.ontologyTermSynonymMetaData = requireNonNull(ontologyTermSynonymMetaData);
		this.ontologyTermDynamicAnnotationMetaData = requireNonNull(ontologyTermDynamicAnnotationMetaData);
		this.ontologyTermNodePathMetaData = requireNonNull(ontologyTermNodePathMetaData);
		this.ontologyMetaData = requireNonNull(ontologyMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Ontology term hit");
		setPackage(ontologyPackage);

		addAttribute(ID, ROLE_ID).setAuto(true);
		addAttribute(SCORE).setDataType(DECIMAL);
		addAttribute(COMBINED_SCORE).setDataType(DECIMAL);

		// Append OntologyTermMetaData attributes with the same name (required by SORTA)
		addAttribute(ONTOLOGY_TERM_IRI).setNillable(false);
		addAttribute(ONTOLOGY_TERM_NAME, ROLE_LABEL).setDataType(TEXT).setNillable(false);
		addAttribute(ONTOLOGY_TERM_SYNONYM).setDataType(MREF)
										   .setNillable(true)
										   .setRefEntity(ontologyTermSynonymMetaData);
		addAttribute(ONTOLOGY_TERM_DYNAMIC_ANNOTATION).setDataType(MREF)
													  .setNillable(true)
													  .setRefEntity(ontologyTermDynamicAnnotationMetaData);
		addAttribute(ONTOLOGY_TERM_NODE_PATH).setDataType(MREF)
											 .setNillable(true)
											 .setRefEntity(ontologyTermNodePathMetaData);
		addAttribute(ONTOLOGY).setDataType(XREF).setNillable(false).setRefEntity(ontologyMetaData);
	}
}
