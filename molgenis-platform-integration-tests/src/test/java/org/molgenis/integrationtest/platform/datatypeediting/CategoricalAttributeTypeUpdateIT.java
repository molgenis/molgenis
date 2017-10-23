package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class CategoricalAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setup()
	{
		super.setup(CATEGORICAL, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(CATEGORICAL);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		Entity entity = dataService.findOneById("REFERENCEENTITY", "1");
		return new Object[][] { { entity, STRING, "1" }, { entity, INT, 1 }, { entity, LONG, 1L },
				{ entity, XREF, "label1" } };
	}

	/**
	 * Valid conversion cases for CATEGORICAL to:
	 * STRING, INT, LONG, XREF
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(Entity valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		Entity entity1 = dataService.findOneById("REFERENCEENTITY", "1");
		Entity entity2 = dataService.findOneById("REFERENCEENTITY", "molgenis@test.org");
		return new Object[][] { { entity1, BOOL, MolgenisValidationException.class,
				"Attribute data type update from [CATEGORICAL] to [BOOL] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, TEXT, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [TEXT] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, SCRIPT, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [SCRIPT] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity2, INT, MolgenisValidationException.class,
						"Value [molgenis@test.org] of this entity attribute is not of type [INT or LONG]." },
				{ entity2, LONG, MolgenisValidationException.class,
						"Value [molgenis@test.org] of this entity attribute is not of type [INT or LONG]." },
				{ entity1, DECIMAL, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [DECIMAL] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [EMAIL] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [HYPERLINK] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, HTML, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [HTML] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, ENUM, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [ENUM] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, DATE, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [DATE] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, DATE_TIME, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [DATE_TIME] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, MREF, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [MREF] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [CATEGORICAL_MREF] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, FILE, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [FILE] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, COMPOUND, MolgenisValidationException.class,
						"Attribute data type update from [CATEGORICAL] to [COMPOUND] not allowed, allowed types are [INT, LONG, STRING, XREF]" },
				{ entity1, ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for CATEGORICAL to:
	 * BOOL, TEXT, SCRIPT INT, LONG, DECIMAL, EMAIL, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(Entity valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
			String exceptionMessage)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (Exception exception)
		{
			assertTrue(exception.getClass().isAssignableFrom(exceptionClass));
			assertEquals(exception.getMessage(), exceptionMessage);
		}
	}
}
