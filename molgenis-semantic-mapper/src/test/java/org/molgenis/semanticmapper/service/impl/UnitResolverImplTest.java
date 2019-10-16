package org.molgenis.semanticmapper.service.impl;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static javax.measure.unit.Unit.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.semanticmapper.service.impl.UnitResolverImpl.UNIT_ONTOLOGY_IRI;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = UnitResolverImplTest.Config.class)
class UnitResolverImplTest extends AbstractMolgenisSpringTest {

  @Autowired private AttributeFactory attrMetaFactory;

  @Autowired private UnitResolverImpl unitResolverImpl;

  @Test
  void testConvertNumberToOntologyTermStyle() {
    assertEquals("kg\\/m\\^\\[2\\]", unitResolverImpl.convertNumberToOntologyTermStyle("kg/m^2"));
    assertEquals("kg\\/m\\^\\[2\\]", unitResolverImpl.convertNumberToOntologyTermStyle("kg/m^²"));
    assertEquals("kg\\/m\\^\\[2\\]", unitResolverImpl.convertNumberToOntologyTermStyle("kg/m²"));
  }

  @Test
  void testTokenize() {
    Set<String> tokenize = unitResolverImpl.tokenize("area density (kg/m^²)");
    assertTrue(newHashSet("area", "density", "kg/m^²").containsAll(tokenize));

    Set<String> tokenize1 = unitResolverImpl.tokenize("area density (kg/m^2)");
    assertTrue(newHashSet("area", "density", "kg/m^²").containsAll(tokenize1));

    Set<String> tokenize2 = unitResolverImpl.tokenize("area density (kg/m2)");
    assertTrue(newHashSet("area", "density", "kg/m²").containsAll(tokenize2));

    Set<String> tokenize3 = unitResolverImpl.tokenize("area 2 density2 (kg/m2)");
    assertTrue(newHashSet("area", "density²", "kg/m²").containsAll(tokenize3));

    Set<String> tokenize4 = unitResolverImpl.tokenize("area 2 density 2 (kg/m2)");
    assertEquals(3, tokenize4.size());
    assertFalse(tokenize4.containsAll(newHashSet("area", "density", "²", "kg/m²")));
  }

  @Test
  void testIsUnitEmpty() {
    Unit<?> unit = Unit.valueOf("");
    Unit<?> unit1 = Unit.valueOf("¹");
    Unit<?> unitKg = Unit.valueOf("kg");
    assertTrue(unitResolverImpl.isUnitEmpty(null));
    assertTrue(unitResolverImpl.isUnitEmpty(unit));
    assertTrue(unitResolverImpl.isUnitEmpty(unit1));
    assertFalse(unitResolverImpl.isUnitEmpty(unitKg));
  }

  @Test
  void testReplaceIllegalChars() {
    assertEquals(
        "area density  kg/m^2 ", unitResolverImpl.replaceIllegalChars("area density (kg/m^2)"));
    assertEquals(
        "area density  kg/m2 ", unitResolverImpl.replaceIllegalChars("area density (kg/m²)"));
  }

  @Test
  void resolveUnitLabelNoUnit() {
    Attribute attr =
        attrMetaFactory.create().setName("attr").setLabel("weight").setDescription(null);
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertNull(unit);
  }

  @Test
  void resolveUnitLabelNoUnitDescriptionNoUnit() {
    Attribute attr =
        attrMetaFactory.create().setName("attr").setLabel("weight").setDescription("weight");
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertNull(unit);
  }

  @Test
  void resolveUnitLabelWithUnit_directUnitMatch() {
    Attribute attr =
        attrMetaFactory.create().setName("attr").setLabel("weight (kg)").setDescription(null);
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertEquals(valueOf("kg"), unit);
  }

  @Test
  void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatch() {
    Attribute attr =
        attrMetaFactory.create().setName("attr").setLabel("label").setDescription("height (cm)");
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertEquals(valueOf("cm"), unit);
  }

  @Test
  void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatchRaw_kgm2() {
    Attribute attr =
        attrMetaFactory
            .create()
            .setName("attr")
            .setLabel("label")
            .setDescription("area density (kg/m2)");
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertEquals(valueOf("kg/m²"), unit);
  }

  @Test
  void resolveUnitLabelNoUnitDescriptionWithUnit_unitOntologyMatch_kgm2() {
    Attribute attr =
        attrMetaFactory
            .create()
            .setName("attr")
            .setLabel("label")
            .setDescription("area density (kg/m^2)");
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertEquals(valueOf("kg/m²"), unit);
  }

  @Test
  void resolveUnitLabelNoUnitDescriptionWithUnit_directUnitMatch_kgm2_2() {
    Attribute attr =
        attrMetaFactory
            .create()
            .setName("attr")
            .setLabel("label")
            .setDescription("area density (kg/m²)");
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertEquals(valueOf("kg/m²"), unit);
  }

  @Test
  void resolveUnitLabelWithUnit_unitOntologyMatch() {
    Attribute attr =
        attrMetaFactory.create().setName("attr").setLabel("weight (kilogram)").setDescription(null);
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertEquals(valueOf("kg"), unit);
  }

  @Test
  void resolveUnitLabelNoUnitDescriptionWithUnit_unitOntologyMatch() {
    Attribute attr =
        attrMetaFactory
            .create()
            .setName("attr")
            .setLabel("label")
            .setDescription("height (centimeter)");
    Unit<? extends Quantity> unit = unitResolverImpl.resolveUnit(attr);
    assertEquals(valueOf("cm"), unit);
  }

  @Configuration
  static class Config {

    @Bean
    UnitResolverImpl unitResolverImpl() {
      return new UnitResolverImpl(ontologyService());
    }

    @Bean
    OntologyService ontologyService() {
      String ontologyId = "id";
      String kgTerm = "kilogram";
      String cmTerm = "centimeter";
      List<String> ontologyIds = singletonList(ontologyId);

      Ontology ontology = Ontology.create(ontologyId, UNIT_ONTOLOGY_IRI, "unit ontology");
      OntologyTerm KG_ONTOLOGY_TERM =
          OntologyTerm.create(UNIT_ONTOLOGY_IRI, kgTerm, asList(kgTerm, "kg"));
      OntologyTerm CM_ONTOLOGY_TERM =
          OntologyTerm.create(UNIT_ONTOLOGY_IRI, cmTerm, asList(cmTerm, "cm"));

      OntologyService ontologyService = mock(OntologyService.class);
      when(ontologyService.getOntology(UNIT_ONTOLOGY_IRI)).thenReturn(ontology);

      when(ontologyService.findExactOntologyTerms(
              ontologyIds, Sets.newLinkedHashSet(asList("weight", "kilogram")), Integer.MAX_VALUE))
          .thenReturn(singletonList(KG_ONTOLOGY_TERM));
      when(ontologyService.findExactOntologyTerms(
              ontologyIds,
              Sets.newLinkedHashSet(asList("label", "height", "centimeter")),
              Integer.MAX_VALUE))
          .thenReturn(singletonList(CM_ONTOLOGY_TERM));

      when(ontologyService.findExactOntologyTerms(
              ontologyIds, newHashSet(kgTerm, cmTerm), Integer.MAX_VALUE))
          .thenReturn(asList(KG_ONTOLOGY_TERM, CM_ONTOLOGY_TERM));
      return ontologyService;
    }
  }
}
