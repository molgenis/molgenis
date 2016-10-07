package org.molgenis.ontology.core.meta;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.AttributeType.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.AttributeType.INT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.ontology.core.meta.OntologyPackage.PACKAGE_ONTOLOGY;

@Component
public class TermFrequencyMetaData extends SystemEntityMetaData
{
	public static final String SIMPLE_NAME = "TermFrequency";
	public static final String TERM_FREQUENCY = PACKAGE_ONTOLOGY + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public final static String ID = "id";
	public final static String TERM = "term";
	public final static String FREQUENCY = "frequency";
	public final static String OCCURRENCE = "occurrence";

	private final OntologyPackage ontologyPackage;

	@Autowired
	TermFrequencyMetaData(OntologyPackage ontologyPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ONTOLOGY);
		this.ontologyPackage = ontologyPackage;
	}

	@Override
	public void init()
	{
		setLabel("Term frequency");
		setPackage(ontologyPackage);

		addAttribute(ID, ROLE_ID).setAuto(true);
		addAttribute(TERM).setNillable(false);
		addAttribute(FREQUENCY).setDataType(DECIMAL).setNillable(false);
		addAttribute(OCCURRENCE).setDataType(INT).setNillable(false);
	}
}