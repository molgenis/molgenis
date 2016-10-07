package org.molgenis.ontology.core.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.ontology.core.meta.OntologyTermMetaData.*;

public class OntologyTermEntity extends StaticEntity
{
	public OntologyTermEntity(Entity entity)
	{
		super(entity);
	}

	public OntologyTermEntity(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public OntologyTermEntity(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getOntologyTermIri()
	{
		return getString(ONTOLOGY_TERM_IRI);
	}

	public void setOntologyTermIri(String ontologyTermIri)
	{
		set(ONTOLOGY_TERM_IRI, ontologyTermIri);
	}

	public String getOntologyTermName()
	{
		return getString(ONTOLOGY_TERM_NAME);
	}

	public void setOntologyTermName(String ontologyTermName)
	{
		set(ONTOLOGY_TERM_NAME, ontologyTermName);
	}

	public Iterable<OntologyTermSynonym> getOntologyTermSynonyms()
	{
		return getEntities(ONTOLOGY_TERM_SYNONYM, OntologyTermSynonym.class);
	}

	public void setOntologyTermSynonyms(Iterable<OntologyTermSynonym> ontologyTermSynonyms)
	{
		set(ONTOLOGY_TERM_SYNONYM, ontologyTermSynonyms);
	}

	public Iterable<OntologyTermDynamicAnnotation> getOntologyTermDynamicAnnotations()
	{
		return getEntities(ONTOLOGY_TERM_DYNAMIC_ANNOTATION, OntologyTermDynamicAnnotation.class);
	}

	public void setOntologyTermDynamicAnnotations(Iterable<OntologyTermDynamicAnnotation> ontologyTermDynamicAnnotation)
	{
		set(ONTOLOGY_TERM_DYNAMIC_ANNOTATION, ontologyTermDynamicAnnotation);
	}

	public Iterable<OntologyTermNodePath> getOntologyTermNodePaths()
	{
		return getEntities(ONTOLOGY_TERM_NODE_PATH, OntologyTermNodePath.class);
	}

	public void setOntologyTermNodePaths(Iterable<OntologyTermNodePath> ontologyTermNodePaths)
	{
		set(ONTOLOGY_TERM_NODE_PATH, ontologyTermNodePaths);
	}

	public Iterable<SemanticTypeEntity> getSemanticTypes()
	{
		return getEntities(ONTOLOGY_TERM_SEMANTIC_TYPE, SemanticTypeEntity.class);
	}

	public void setSemanticTypes(Iterable<SemanticTypeEntity> semanticTypes)
	{
		set(ONTOLOGY_TERM_SEMANTIC_TYPE, semanticTypes);
	}

	public OntologyEntity getOntology()
	{
		return getEntity(ONTOLOGY, OntologyEntity.class);
	}

	public void setOntology(OntologyEntity ontology)
	{
		set(ONTOLOGY, ontology);
	}
}
