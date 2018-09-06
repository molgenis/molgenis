package org.molgenis.data.icd10;

import java.util.Collection;
import org.molgenis.data.Entity;

/** Expands a list of 'eu_bbmri_eric_disease_types' entities to include all of their children. */
public interface Icd10ClassExpander {
  Collection<Entity> expandClasses(Collection<Entity> diseaseClasses);
}
