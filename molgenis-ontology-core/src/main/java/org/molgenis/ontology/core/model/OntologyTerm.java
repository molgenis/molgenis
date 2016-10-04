package org.molgenis.ontology.core.model;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

public interface OntologyTerm
{
	String getIRI();

	String getLabel();

	default List<String> getAtomicIRIs()
	{
		String iri = getIRI();
		return isBlank(iri) ? emptyList() : of(iri.split(",")).collect(toList());
	}
}