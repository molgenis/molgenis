package org.molgenis.data.meta;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LOOKUP;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.AttributeMetadata;
import org.molgenis.data.meta.model.EntityTypeMetadata;
import org.molgenis.data.populate.IdGenerator;

class SystemEntityTypeTest {
  AttributeFactory attributeFactory;
  EntityTypeMetadata entityTypeMetaData;

  @BeforeEach
  void beforeMethod() {
    attributeFactory = mock(AttributeFactory.class);
    entityTypeMetaData = mock(EntityTypeMetadata.class);

    when(attributeFactory.getAttributeMetadata()).thenReturn(mock(AttributeMetadata.class));
  }

  @Test
  void testAssignRolesToAttributeParts() {
    TestCompoundEMD testEMD = new TestCompoundEMD("Test");
    testEMD.setAttributeFactory(attributeFactory);
    testEMD.bootstrap(entityTypeMetaData);

    assertEquals("idAttr", testEMD.getIdAttribute().getName());
    assertEquals("labelAttr", testEMD.getLabelAttribute().getName());

    Set<String> lookupAttributes = newHashSet();
    testEMD.getLookupAttributes().forEach(e -> lookupAttributes.add(e.getName()));
    assertEquals(newHashSet("lookupAttr1", "lookupAttr2"), lookupAttributes);
  }

  @Test
  void testAssignRolesToAttributePartsNested() {
    TestNestedCompoundEMD testEMD = new TestNestedCompoundEMD("Test");
    testEMD.setAttributeFactory(attributeFactory);
    testEMD.bootstrap(entityTypeMetaData);

    assertEquals("idAttr", testEMD.getIdAttribute().getName());
    assertEquals("labelAttr", testEMD.getLabelAttribute().getName());

    Set<String> lookupAttributes = newHashSet();
    testEMD.getLookupAttributes().forEach(e -> lookupAttributes.add(e.getName()));
    assertEquals(newHashSet("lookupAttr1", "lookupAttr2"), lookupAttributes);
  }

  private class TestNestedCompoundEMD extends SystemEntityType {

    TestNestedCompoundEMD(String entityTypeId) {
      super(entityTypeId);
      setIdGenerator(mock(IdGenerator.class));
    }

    @Override
    protected void init() {
      Attribute compoundAttr1 =
          when(mock(Attribute.class).getName()).thenReturn("compound2").getMock();
      when(compoundAttr1.getLookupAttributeIndex()).thenReturn(null);
      addAttribute(compoundAttr1);
      addAttribute("compoundAttr2").setParent(compoundAttr1);
      addAttribute("idAttr", ROLE_ID).setParent(compoundAttr1);
      addAttribute("labelAttr", ROLE_LABEL).setParent(compoundAttr1);
      addAttribute("lookupAttr1", ROLE_LOOKUP).setParent(compoundAttr1);
      addAttribute("lookupAttr2", ROLE_LOOKUP).setParent(compoundAttr1);
    }
  }

  private class TestCompoundEMD extends SystemEntityType {

    TestCompoundEMD(String entityTypeId) {
      super(entityTypeId);
      setIdGenerator(mock(IdGenerator.class));
    }

    @Override
    protected void init() {
      Attribute compoundAttr =
          when(mock(Attribute.class).getName()).thenReturn("compound").getMock();
      when(compoundAttr.getLookupAttributeIndex()).thenReturn(null);
      addAttribute(compoundAttr);
      addAttribute("idAttr", ROLE_ID).setParent(compoundAttr);
      addAttribute("labelAttr", ROLE_LABEL).setParent(compoundAttr);
      addAttribute("lookupAttr1", ROLE_LOOKUP).setParent(compoundAttr);
      addAttribute("lookupAttr2", ROLE_LOOKUP).setParent(compoundAttr);
    }
  }
}
