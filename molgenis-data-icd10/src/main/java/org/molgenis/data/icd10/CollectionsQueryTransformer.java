package org.molgenis.data.icd10;

import org.molgenis.data.Entity;
import org.molgenis.data.Query;

/** Transforms a query on an attribute that refers to a ICD-10 repository */
public interface CollectionsQueryTransformer {
  /**
   * @param query the query to transform
   * @param icd10EntityTypeId the EntityType that contains the ICD-10 identifiers
   * @param expandAttribute the attribute on which to apply the query transformation
   * @return a query containing expanded ICD-10 codes
   */
  Query<Entity> transformQuery(
      Query<Entity> query, String icd10EntityTypeId, String expandAttribute);
}
