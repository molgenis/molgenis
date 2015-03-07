package org.molgenis.ontology.repository;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public abstract class AbstractOntologyRepository extends AbstractRepository
{
	protected DefaultEntityMetaData entityMetaData = null;
	protected final String entityName;
	public final static String ID = "id";
	public final static String NODE_PATH = "nodePath";
	public final static String PARENT_NODE_PATH = "parentNodePath";
	public final static String PARENT_ONTOLOGY_TERM_IRI = "parentOntologyTermIRI";
	public final static String FIELDTYPE = "fieldType";
	public final static String ROOT = "root";
	public final static String LAST = "isLast";
	public final static String ONTOLOGY_IRI = "ontologyIRI";
	public final static String ONTOLOGY_NAME = "ontologyName";
	public final static String ONTOLOGY_TERM = "ontologyTerm";
	public final static String ONTOLOGY_TERM_DEFINITION = "definition";
	public final static String ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	public final static String SYNONYMS = "ontologyTermSynonym";
	public final static String ALTERNATIVE_DEFINITION = "alternativeDefinition";
	public final static String ENTITY_TYPE = "entity_type";
	public final static String TYPE_ONTOLOGYTERM = "ontologyTerm";
	public final static String CHIDLREN = "children";
	public final static String ILLEGAL_CHARACTERS_PATTERN = "[^a-zA-Z0-9 ]";
	public final static String ILLEGAL_CHARACTERS_REPLACEMENT = "\\s";
	public final static String NODE_PATH_REPLACEMENT_PATTERN = "\\.[0-9]+$";
	public final static String MULTI_WHITESPACES = " +";
	public final static String SINGLE_WHITESPACE = " ";

	public AbstractOntologyRepository(String entityName)
	{
		if (StringUtils.isEmpty(entityName)) throw new IllegalArgumentException("The ontology entityName is null!");
		this.entityName = entityName;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(entityName);
			DefaultAttributeMetaData attributeId = new DefaultAttributeMetaData(ID);
			attributeId.setIdAttribute(true);
			entityMetaData.addAttributeMetaData(attributeId);
			entityMetaData
					.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_IRI, FieldTypeEnum.HYPERLINK));
			DefaultAttributeMetaData attributeMetaData = new DefaultAttributeMetaData(ONTOLOGY_TERM);
			attributeMetaData.setLabelAttribute(true);
			entityMetaData.addAttributeMetaData(attributeMetaData);
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(SYNONYMS));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ENTITY_TYPE));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(NODE_PATH));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(PARENT_NODE_PATH));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(PARENT_ONTOLOGY_TERM_IRI));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(FIELDTYPE));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(LAST, FieldTypeEnum.BOOL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ROOT, FieldTypeEnum.BOOL));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_TERM_DEFINITION));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_NAME));
			entityMetaData.addAttributeMetaData(new DefaultAttributeMetaData(ONTOLOGY_IRI, FieldTypeEnum.HYPERLINK));
			DefaultAttributeMetaData childrenAttributeMetaData = new DefaultAttributeMetaData("attributes",
					FieldTypeEnum.MREF);
			childrenAttributeMetaData.setRefEntity(entityMetaData);
			entityMetaData.addAttributeMetaData(childrenAttributeMetaData);
		}
		return entityMetaData;
	}

	@Override
	public String getName()
	{
		return getEntityMetaData().getName();
	}

}
