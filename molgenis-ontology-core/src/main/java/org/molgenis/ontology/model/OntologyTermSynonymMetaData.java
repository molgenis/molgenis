package org.molgenis.ontology.model;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class OntologyTermSynonymMetaData extends DefaultEntityMetaData
{
	public final static String ID = "id";
	public final static String ONTOLOGY_TERM_SYNONYM = "ontologyTermSynonym";
	public final static String SIMPLE_NAME = "OntologyTermSynonym";
	public final static String ENTITY_NAME = OntologyPackage.PACKAGE_NAME + "_" + SIMPLE_NAME;
	public final static OntologyTermSynonymMetaData INSTANCE = new OntologyTermSynonymMetaData();

	private OntologyTermSynonymMetaData()
	{
		super(SIMPLE_NAME, OntologyPackage.getPackageInstance());
		addAttributeMetaData(new DefaultAttributeMetaData(ID).setIdAttribute(true).setNillable(false).setVisible(false));

		DefaultAttributeMetaData ontologyTermSynonymAttr = new DefaultAttributeMetaData(ONTOLOGY_TERM_SYNONYM,
				FieldTypeEnum.STRING).setNillable(false).setLabelAttribute(true);
		addAttributeMetaData(ontologyTermSynonymAttr);
	}
}