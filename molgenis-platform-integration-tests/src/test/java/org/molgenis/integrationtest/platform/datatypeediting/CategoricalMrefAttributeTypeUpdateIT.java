package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class CategoricalMrefAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(CATEGORICAL_MREF, INT);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(CATEGORICAL_MREF);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		List<Entity> entities = dataService.findAll("REFERENCEENTITY").collect(toList());
		return new Object[][] { { entities, MREF, newArrayList("label1", "email label", "hyperlink label") } };
	}

	/**
	 * Valid conversion cases for CATEGORICAL_MREF to:
	 * MREF
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(List<Entity> valueToConvert, AttributeType typeToConvertTo,
			List<String> convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);

		List<String> actualValues = newArrayList();
		Entity entity1 = dataService.findOneById("MAINENTITY", "1");
		entity1.getEntities("mainAttribute").forEach(entity -> actualValues.add(entity.getLabelValue().toString()));
		assertEquals(actualValues, convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		List<Entity> entities = dataService.findAll("REFERENCEENTITY").collect(toList());
		return new Object[][] { { entities, BOOL, MolgenisDataException.class,
				"Attribute data type update from [CATEGORICAL_MREF] to [BOOL] not allowed, allowed types are [MREF]" },
				{ entities, STRING, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [STRING] not allowed, allowed types are [MREF]" },
				{ entities, TEXT, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [TEXT] not allowed, allowed types are [MREF]" },
				{ entities, SCRIPT, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [SCRIPT] not allowed, allowed types are [MREF]" },
				{ entities, INT, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [INT] not allowed, allowed types are [MREF]" },
				{ entities, LONG, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [LONG] not allowed, allowed types are [MREF]" },
				{ entities, DECIMAL, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [DECIMAL] not allowed, allowed types are [MREF]" },
				{ entities, XREF, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [XREF] not allowed, allowed types are [MREF]" },
				{ entities, CATEGORICAL, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [CATEGORICAL] not allowed, allowed types are [MREF]" },
				{ entities, EMAIL, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [EMAIL] not allowed, allowed types are [MREF]" },
				{ entities, HYPERLINK, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [HYPERLINK] not allowed, allowed types are [MREF]" },
				{ entities, HTML, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [HTML] not allowed, allowed types are [MREF]" },
				{ entities, ENUM, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [ENUM] not allowed, allowed types are [MREF]" },
				{ entities, DATE, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [DATE] not allowed, allowed types are [MREF]" },
				{ entities, DATE_TIME, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [DATE_TIME] not allowed, allowed types are [MREF]" },
				{ entities, FILE, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [FILE] not allowed, allowed types are [MREF]" },
				{ entities, COMPOUND, MolgenisDataException.class,
						"Attribute data type update from [CATEGORICAL_MREF] to [COMPOUND] not allowed, allowed types are [MREF]" },
				{ entities, ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for CATEGORICAL_MREF to:
	 * BOOL, STRING, TEXT, SCRIPT INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(List<Object> valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
			String exceptionMessage)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (Exception exception)
		{
			System.out.println(exception.getClass());
			assertTrue(exception.getClass().isAssignableFrom(exceptionClass));
			assertEquals(exception.getMessage(), exceptionMessage);
		}
	}
}
