package org.molgenis.api.data.v3;

import org.testng.annotations.Test;

// TODO decide which test cases to move to controller unit test class
// 0. check api tests of v1 and v2 and include relevant test cases
// 1. all data types (except html/hyperlink etc.)
// 2. null values
// 3. computed (for update/create)
// 4. mappedBy (for update/create)
// 5. compounds
// 6. abstract: abstract entity types + concrete entity types that extend from abstract entity type
// 7. transactions
// 8. exceptions (including security)
// 9. different identifier data types (also for references)
// 10. request zero-items case
// 11. error codes
// 12. encoding entity type identifiers, entity identifiers etc.
// 13 de REST hack
public class ApiIT {

  @Test
  public void testRetrieveResource() {
    // 1. filter (basic? multiple? nested?)
    // 2. expand
    // 3. query
    // 4. sort
    // exc: entity type does not exist
    // exc: entity does not exist
    // exc: no permission to read entity type
    throw new UnsupportedOperationException();
  }

  @Test
  public void testRetrieveResourceSubResource() {
    // exc: entity type exists
    // exc: entity exists
    // exc: attribute does not exist
    throw new UnsupportedOperationException();

  }

  @Test
  public void testRetrieveResourceCollection() {
    // 1. paging
    // 2. links: first page and last page
    // exc: max page size exceeded
    throw new UnsupportedOperationException();

  }

  @Test
  public void createResource() {
    // 1. location header
    // exc: already exists
    throw new UnsupportedOperationException();

  }

  @Test
  public void updateResource() {
    throw new UnsupportedOperationException();

  }

  @Test
  public void partialUpdateResource() {
    throw new UnsupportedOperationException();
  }

  @Test
  public void deleteResource() {
    throw new UnsupportedOperationException();
  }

  @Test
  public void deleteResourceCollection() {
    throw new UnsupportedOperationException();
  }
}
