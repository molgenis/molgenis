package org.molgenis.semanticmapper.mapping.model;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.molgenis.semanticmapper.mapping.model.AttributeMapping.createCopy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.test.AbstractMockitoTest;

class AttributeMappingTest extends AbstractMockitoTest {
  @Test
  void testCreateCopy() {
    String identifier = "MyIdentifier";
    String targetAttributeName = "MyTargetAttributeName";
    Attribute targetAttribute = mock(Attribute.class);
    String algorithm = "MyAlgorithm";
    List<Attribute> sourceAttributes = emptyList();
    String algorithmState = AlgorithmState.CURATED.name();

    AttributeMapping attributeMapping =
        new AttributeMapping(
            identifier,
            targetAttributeName,
            targetAttribute,
            algorithm,
            sourceAttributes,
            algorithmState);
    assertEquals(attributeMapping, createCopy(attributeMapping));
  }
}
