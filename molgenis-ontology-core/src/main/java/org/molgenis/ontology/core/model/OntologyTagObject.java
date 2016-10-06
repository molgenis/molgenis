package org.molgenis.ontology.core.model;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Ontology term or combined ontology terms that can be used to tag a subject with.
 */
public interface OntologyTagObject
{
	String getIRI();

	String getLabel();

	default List<String> getAtomicIRIs()
	{
		String iri = getIRI();
		return isBlank(iri) ? emptyList() : of(iri.split(",")).collect(toList());
	}
}