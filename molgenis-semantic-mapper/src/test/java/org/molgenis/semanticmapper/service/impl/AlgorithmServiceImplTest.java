package org.molgenis.semanticmapper.service.impl;

import com.google.common.collect.Lists;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.semanticmapper.algorithmgenerator.service.AlgorithmGeneratorService;
import org.molgenis.semanticmapper.mapping.model.AttributeMapping;
import org.molgenis.semanticsearch.service.OntologyTagService;
import org.molgenis.semanticsearch.service.SemanticSearchService;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
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

	@Test
	public void testApplyAlgorithm()
	{
		Attribute attribute = mock(Attribute.class);
		String algorithm = "algorithm";
		Entity entity = mock(Entity.class);

		Mockito.when(jsMagmaScriptEvaluator.eval(algorithm, entity)).thenThrow(new NullPointerException());

		Iterable<AlgorithmEvaluation> result = algorithmServiceImpl.applyAlgorithm(attribute, algorithm,
				Lists.newArrayList(entity));
		AlgorithmEvaluation eval = result.iterator().next();

		Assert.assertEquals(eval.getErrorMessage(),
				"Applying an algorithm on a null source value caused an exception. Is the target attribute required?");
	}

	private void testApplyConvertException(String algorithmResult, AttributeType attributeType)
	{
		AttributeMapping attributeMapping = mock(AttributeMapping.class);
		String algorithm = "algorithm";
		Mockito.when(attributeMapping.getAlgorithm()).thenReturn(algorithm);
		Attribute targetAttribute = Mockito.when(mock(Attribute.class).getDataType())
										   .thenReturn(attributeType)
										   .getMock();
		Mockito.when(attributeMapping.getTargetAttribute()).thenReturn(targetAttribute);

		Entity sourceEntity = mock(Entity.class);
		Mockito.when(jsMagmaScriptEvaluator.eval(algorithm, sourceEntity)).thenReturn(algorithmResult);

		algorithmServiceImpl.apply(attributeMapping, sourceEntity, null);
	}
}