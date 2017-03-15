package org.molgenis.data.mapper.service.impl;

import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.mapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.test.AbstractMockitoTest;
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

	@Test(expectedExceptions = AlgorithmException.class, expectedExceptionsMessageRegExp = "'invalidDate' can't be converted to type 'DATE'")
	public void testApplyConvertDateNumberFormatException()
	{
		testApplyConvertException("invalidDate", DATE);
	}

	@Test(expectedExceptions = AlgorithmException.class, expectedExceptionsMessageRegExp = "'invalidDateTime' can't be converted to type 'DATE_TIME'")
	public void testApplyConvertDateTimeNumberFormatException()
	{
		testApplyConvertException("invalidDateTime", DATE_TIME);
	}

	@Test(expectedExceptions = AlgorithmException.class, expectedExceptionsMessageRegExp = "'invalidDouble' can't be converted to type 'DECIMAL'")
	public void testApplyConvertDoubleNumberFormatException()
	{
		testApplyConvertException("invalidDouble", DECIMAL);
	}

	@Test(expectedExceptions = AlgorithmException.class, expectedExceptionsMessageRegExp = "'invalidInt' can't be converted to type 'INT'")
	public void testApplyConvertIntNumberFormatException()
	{
		testApplyConvertException("invalidInt", INT);
	}

	@Test(expectedExceptions = AlgorithmException.class, expectedExceptionsMessageRegExp = "'9007199254740991' is larger than the maximum allowed value for type 'INT'")
	public void testApplyConvertIntArithmeticException()
	{
		testApplyConvertException("9007199254740991", INT);
	}

	@Test(expectedExceptions = AlgorithmException.class, expectedExceptionsMessageRegExp = "'invalidLong' can't be converted to type 'LONG'")
	public void testApplyConvertLongNumberFormatException()
	{
		testApplyConvertException("invalidLong", LONG);
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