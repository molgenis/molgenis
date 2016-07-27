package org.molgenis.ontology.core.meta;

import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ID;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_NAME;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class OntologyTerm extends StaticEntity
{
	public OntologyTerm(Entity entity)
	{
		super(entity);
	}

	public OntologyTerm(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public OntologyTerm(String id, EntityMetaData entityMeta)
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

	public Ontology getOntology()
	{
		return getEntity(ONTOLOGY, Ontology.class);
	}

	public void setOntology(Ontology ontology)
	{
		set(ONTOLOGY, ontology);
	}
}
