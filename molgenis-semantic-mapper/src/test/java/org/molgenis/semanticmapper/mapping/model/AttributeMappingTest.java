package org.molgenis.semanticmapper.mapping.model;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.util.List;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

public class AttributeMappingTest extends AbstractMockitoTest {
  @Test
  public void testCreateCopy() {
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
    assertEquals(AttributeMapping.createCopy(attributeMapping), attributeMapping);
  }
}
