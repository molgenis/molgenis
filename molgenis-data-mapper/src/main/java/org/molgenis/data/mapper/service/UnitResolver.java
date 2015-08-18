package org.molgenis.data.mapper.service;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.ontology.core.model.OntologyTerm;

public interface UnitResolver
{
	/**
	 * 
	 * @param attr
	 *            attribute for which to determine unit
	 * @param entityMeta
	 *            corresponding entity meta data for attribute
	 * @param repo
	 *            entity data (can be null)
	 * @return unit ontology term or null
	 */
	OntologyTerm resolveUnit(AttributeMetaData attr, EntityMetaData entityMeta, Repository repo);
}
