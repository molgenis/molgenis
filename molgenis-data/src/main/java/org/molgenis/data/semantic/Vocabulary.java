package org.molgenis.data.semantic;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class Vocabulary {
  private Vocabulary() {}

  /** http://purl.obolibrary.org/obo/NCIT_C71490 */
  public static final IRI CASE_SENSITIVE =
      SimpleValueFactory.getInstance().createIRI("http://purl.obolibrary.org/obo/NCIT_C71490");

  public static final IRI AUDITED =
      SimpleValueFactory.getInstance().createIRI("http://molgenis.org/vocab/audit/audited");
}
