package org.molgenis.semanticmapper.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;

import com.google.common.collect.Lists;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.script.core.ScriptException;
import org.molgenis.semanticmapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping.AlgorithmState;
import org.molgenis.semanticmapper.mapping.model.EntityMapping;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.test.AbstractMockitoTest;

class AlgorithmServiceImplTest extends AbstractMockitoTest {
  @Mock private OntologyTagService ontologyTagService;
  @Mock private SemanticSearchService semanticSearhService;
  @Mock private AlgorithmGeneratorService algorithmGeneratorService;
  @Mock private EntityManager entityManager;
  @Mock private JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

  private AlgorithmServiceImpl algorithmServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    algorithmServiceImpl =
        new AlgorithmServiceImpl(
            semanticSearhService, algorithmGeneratorService, entityManager, jsMagmaScriptEvaluator);
  }

  @Test
  void testAlgorithmServiceImpl() {
    assertThrows(
        NullPointerException.class, () -> new AlgorithmServiceImpl(null, null, null, null));
  }

  @Test
  void testApplyConvertDateNumberFormatException() {
    Exception exception =
        assertThrows(
            AlgorithmException.class, () -> testApplyConvertException("invalidDate", DATE));
    assertThat(exception.getMessage())
        .containsPattern("'invalidDate' can't be converted to type 'DATE'");
  }

  @Test
  void testApplyConvertDateTimeNumberFormatException() {
    Exception exception =
        assertThrows(
            AlgorithmException.class,
            () -> testApplyConvertException("invalidDateTime", DATE_TIME));
    assertThat(exception.getMessage())
        .containsPattern("'invalidDateTime' can't be converted to type 'DATE_TIME'");
  }

  @Test
  void testApplyConvertDoubleNumberFormatException() {
    Exception exception =
        assertThrows(
            AlgorithmException.class, () -> testApplyConvertException("invalidDouble", DECIMAL));
    assertThat(exception.getMessage())
        .containsPattern("'invalidDouble' can't be converted to type 'DECIMAL'");
  }

  @Test
  void testApplyConvertIntNumberFormatException() {
    Exception exception =
        assertThrows(AlgorithmException.class, () -> testApplyConvertException("invalidInt", INT));
    assertThat(exception.getMessage())
        .containsPattern("'invalidInt' can't be converted to type 'INT'");
  }

  @Test
  void testApplyConvertIntArithmeticException() {
    Exception exception =
        assertThrows(
            AlgorithmException.class, () -> testApplyConvertException("9007199254740991", INT));
    assertThat(exception.getMessage())
        .containsPattern(
            "'9007199254740991' is larger than the maximum allowed value for type 'INT'");
  }

  @Test
  void testApplyConvertLongNumberFormatException() {
    Exception exception =
        assertThrows(
            AlgorithmException.class, () -> testApplyConvertException("invalidLong", LONG));
    assertThat(exception.getMessage())
        .containsPattern("'invalidLong' can't be converted to type 'LONG'");
  }

  @Test
  void testApplyAlgorithm() {
    Attribute attribute = mock(Attribute.class);
    String algorithm = "algorithm";
    Entity entity = mock(Entity.class);

    when(jsMagmaScriptEvaluator.eval(algorithm, entity, 3)).thenThrow(new NullPointerException());

    Iterable<AlgorithmEvaluation> result =
        algorithmServiceImpl.applyAlgorithm(attribute, algorithm, Lists.newArrayList(entity), 3);
    AlgorithmEvaluation eval = result.iterator().next();

    assertEquals(
        eval.getErrorMessage(),
        "Applying an algorithm on a null source value caused an exception. Is the target attribute required?");
  }

  @Test
  void testApplyAlgorithmWitInvalidScript() {
    Attribute attribute = mock(Attribute.class);
    String algorithm = "algorithm";
    Entity entity = mock(Entity.class);

    when(jsMagmaScriptEvaluator.eval(algorithm, entity, 3))
        .thenReturn(new ScriptException("algorithm is not defined"));
    Iterable<AlgorithmEvaluation> result =
        algorithmServiceImpl.applyAlgorithm(attribute, algorithm, Lists.newArrayList(entity), 3);
    AlgorithmEvaluation eval = result.iterator().next();
    assertEquals(eval.getErrorMessage(), "algorithm is not defined");
  }

  @Test
  void testApplyWithInvalidScript() {
    AttributeMapping attributeMapping = mock(AttributeMapping.class);
    String algorithm = "algorithm";
    when(attributeMapping.getAlgorithm()).thenReturn(algorithm);

    Entity sourceEntity = mock(Entity.class);
    when(jsMagmaScriptEvaluator.eval(algorithm, sourceEntity, 3))
        .thenReturn(new ScriptException("algorithm is not defined"));
    Exception exception =
        assertThrows(
            AlgorithmException.class,
            () -> algorithmServiceImpl.apply(attributeMapping, sourceEntity, null, 3));
    assertThat(exception.getMessage())
        .containsPattern("org.molgenis.script.core.ScriptException: algorithm is not defined");
  }

  @Test
  void testCopyAlgorithms() {
    EntityMapping sourceEntityMapping = mock(EntityMapping.class);
    AttributeMapping attributeMapping = mock(AttributeMapping.class);
    when(attributeMapping.getIdentifier()).thenReturn("MyIdentifier");
    when(attributeMapping.getAlgorithmState()).thenReturn(AlgorithmState.CURATED);
    when(sourceEntityMapping.getAttributeMappings())
        .thenReturn(Collections.singletonList(attributeMapping));
    EntityMapping targetEntityMapping = mock(EntityMapping.class);

    algorithmServiceImpl.copyAlgorithms(sourceEntityMapping, targetEntityMapping);

    ArgumentCaptor<AttributeMapping> attributeMappingCaptor =
        ArgumentCaptor.forClass(AttributeMapping.class);
    verify(targetEntityMapping).addAttributeMapping(attributeMappingCaptor.capture());
    AttributeMapping attributeMappingCopy = attributeMappingCaptor.getValue();
    assertNull(attributeMappingCopy.getIdentifier());
    assertEquals(attributeMappingCopy.getAlgorithmState(), AlgorithmState.DISCUSS);
  }

  private void testApplyConvertException(String algorithmResult, AttributeType attributeType) {
    AttributeMapping attributeMapping = mock(AttributeMapping.class);
    String algorithm = "algorithm";
    when(attributeMapping.getAlgorithm()).thenReturn(algorithm);
    Attribute targetAttribute =
        when(mock(Attribute.class).getDataType()).thenReturn(attributeType).getMock();
    when(attributeMapping.getTargetAttribute()).thenReturn(targetAttribute);

    Entity sourceEntity = mock(Entity.class);
    when(jsMagmaScriptEvaluator.eval(algorithm, sourceEntity, 3)).thenReturn(algorithmResult);

    algorithmServiceImpl.apply(attributeMapping, sourceEntity, null, 3);
  }
}
