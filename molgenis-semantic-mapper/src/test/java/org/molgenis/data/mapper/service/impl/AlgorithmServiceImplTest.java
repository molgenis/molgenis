package org.molgenis.data.mapper.service.impl;

import com.google.common.collect.Lists;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.exception.ValueConversionException;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.AttributeType.*;

public class AlgorithmServiceImplTest extends AbstractMockitoTest
{
	@Mock
	private OntologyTagService ontologyTagService;
	@Mock
	private SemanticSearchService semanticSearhService;
	@Mock
	private AlgorithmGeneratorService algorithmGeneratorService;
	@Mock
	private EntityManager entityManager;
	@Mock
	private JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

	private AlgorithmServiceImpl algorithmServiceImpl;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		algorithmServiceImpl = new AlgorithmServiceImpl(ontologyTagService, semanticSearhService,
				algorithmGeneratorService, entityManager, jsMagmaScriptEvaluator);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testAlgorithmServiceImpl()
	{
		new AlgorithmServiceImpl(null, null, null, null, null);
	}

	@Test(expectedExceptions = ValueConversionException.class, expectedExceptionsMessageRegExp = "value:invalidDate type:DATE")
	public void testApplyConvertDateNumberFormatException()
	{
		testApplyConvertException("invalidDate", DATE);
	}

	@Test(expectedExceptions = ValueConversionException.class, expectedExceptionsMessageRegExp = "value:invalidDateTime type:DATE_TIME")
	public void testApplyConvertDateTimeNumberFormatException()
	{
		testApplyConvertException("invalidDateTime", DATE_TIME);
	}

	@Test(expectedExceptions = ValueConversionException.class, expectedExceptionsMessageRegExp = "value:invalidDouble type:DECIMAL")
	public void testApplyConvertDoubleNumberFormatException()
	{
		testApplyConvertException("invalidDouble", DECIMAL);
	}

	@Test(expectedExceptions = ValueConversionException.class, expectedExceptionsMessageRegExp = "value:invalidInt type:INT")
	public void testApplyConvertIntNumberFormatException()
	{
		testApplyConvertException("invalidInt", INT);
	}

	@Test(expectedExceptions = ValueConversionException.class, expectedExceptionsMessageRegExp = "value:9007199254740991 type:INT")
	public void testApplyConvertIntArithmeticException()
	{
		testApplyConvertException("9007199254740991", INT);
	}

	@Test(expectedExceptions = ValueConversionException.class, expectedExceptionsMessageRegExp = "value:invalidLong type:LONG")
	public void testApplyConvertLongNumberFormatException()
	{
		testApplyConvertException("invalidLong", LONG);
	}

	@Test
	public void testApplyAlgorithm()
	{
		Attribute attribute = mock(Attribute.class);
		String algorithm = "algorithm";
		Entity entity = mock(Entity.class);

		when(jsMagmaScriptEvaluator.eval(algorithm, entity)).thenThrow(new NullPointerException());

		Iterable<AlgorithmEvaluation> result = algorithmServiceImpl.applyAlgorithm(attribute, algorithm,
				Lists.newArrayList(entity));
		AlgorithmEvaluation eval = result.iterator().next();

		Assert.assertEquals(eval.getErrorMessage(), "null");
	}

	private void testApplyConvertException(String algorithmResult, AttributeType attributeType)
	{
		AttributeMapping attributeMapping = mock(AttributeMapping.class);
		String algorithm = "algorithm";
		when(attributeMapping.getAlgorithm()).thenReturn(algorithm);
		Attribute targetAttribute = when(mock(Attribute.class).getDataType()).thenReturn(attributeType).getMock();
		when(attributeMapping.getTargetAttribute()).thenReturn(targetAttribute);

		Entity sourceEntity = mock(Entity.class);
		when(jsMagmaScriptEvaluator.eval(algorithm, sourceEntity)).thenReturn(algorithmResult);

		algorithmServiceImpl.apply(attributeMapping, sourceEntity, null);
	}
}