package org.molgenis.data.support;

import com.google.gson.JsonSyntaxException;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.util.EntityUtils;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertTrue;

public class MapOfStringsExpressionEvaluatorTest extends AbstractMockitoTest
{
	@Mock
	private Attribute attribute;
	@Mock
	private EntityType entityType;

	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Attribute has no expression.")
	public void testMapOfStringsExpressionEvaluatorAttributeWithoutExpression()
	{
		new MapOfStringsExpressionEvaluator(attribute, entityType);
	}

	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "refEntity not specified.")
	public void testMapOfStringsExpressionEvaluatorAttributeWithoutRefEntity()
	{
		when(attribute.getExpression()).thenReturn("{'a':b}");
		new MapOfStringsExpressionEvaluator(attribute, entityType);
	}

	@Test(expectedExceptions = JsonSyntaxException.class)
	public void testMapOfStringsExpressionEvaluatorExpressionSyntaxError()
	{
		EntityType refEntityType = mock(EntityType.class);
		when(attribute.getRefEntity()).thenReturn(refEntityType);
		when(attribute.getExpression()).thenReturn("hallo");
		new MapOfStringsExpressionEvaluator(attribute, entityType);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Unknown target attribute: hallo.")
	public void testMapOfStringsExpressionEvaluatorUnknownTargetAttribute()
	{
		EntityType refEntityType = mock(EntityType.class);
		when(attribute.getRefEntity()).thenReturn(refEntityType);
		when(attribute.getExpression()).thenReturn("{'hallo':String}");
		new MapOfStringsExpressionEvaluator(attribute, entityType);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Expression for attribute 'Chromosome' references non-existant attribute 'hallo'.")
	public void testMapOfStringsExpressionEvaluatorUnknownReferencedAttribute()
	{
		EntityType refEntityType = getRefEntityTypeMock();

		Attribute refAttribute = mock(Attribute.class);
		when(refAttribute.getName()).thenReturn("Chromosome");
		when(refAttribute.getEntityType()).thenReturn(refEntityType);
		doReturn(refAttribute).when(refEntityType).getAttribute("Chromosome");
		when(attribute.getRefEntity()).thenReturn(refEntityType);
		when(attribute.getExpression()).thenReturn("{'Chromosome':hallo}");
		new MapOfStringsExpressionEvaluator(attribute, entityType);
	}

	@Test
	public void testEvaluate()
	{
		EntityType refEntityType = getRefEntityTypeMock();

		when(attribute.getName()).thenReturn("#CHROM").getMock();
		when(attribute.getDataType()).thenReturn(XREF);
		when(attribute.getRefEntity()).thenReturn(refEntityType);
		when(attribute.getExpression()).thenReturn("{'Chromosome':String, 'Position':Int}");
		when(attribute.getEntityType()).thenReturn(mock(EntityType.class));
		when(attribute.getDataType()).thenReturn(XREF);

		ExpressionEvaluator evaluator = new MapOfStringsExpressionEvaluator(attribute, entityType);
		Entity expected = new DynamicEntity(refEntityType);
		expected.set("Chromosome", "12");
		expected.set("Position", "1");
		Entity entity = mock(Entity.class);
		Entity actual = (Entity) evaluator.evaluate(entity);
		assertTrue(EntityUtils.equals(actual, expected));
	}

	private EntityType getRefEntityTypeMock()
	{
		Attribute typeAttribute = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		Attribute nillableAttribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
		Attribute autoAttribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
		Attribute visibleAttribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
		Attribute aggregatableAttribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
		Attribute readOnlyAttribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();
		Attribute uniqueAttribute = when(mock(Attribute.class).getDataType()).thenReturn(BOOL).getMock();

		EntityType refEntityType = mock(EntityType.class);
		doReturn(typeAttribute).when(refEntityType).getAttribute("type");
		doReturn(nillableAttribute).when(refEntityType).getAttribute("isNullable");
		doReturn(autoAttribute).when(refEntityType).getAttribute("isAuto");
		doReturn(visibleAttribute).when(refEntityType).getAttribute("isVisible");
		doReturn(aggregatableAttribute).when(refEntityType).getAttribute("isAggregatable");
		doReturn(readOnlyAttribute).when(refEntityType).getAttribute("isReadOnly");
		doReturn(uniqueAttribute).when(refEntityType).getAttribute("isUnique");

		return refEntityType;
	}
}
