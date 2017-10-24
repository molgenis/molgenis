package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class EnumAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(ENUM, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(ENUM);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "1", STRING, "1" }, { "1", TEXT, "1" }, { "1", INT, 1 }, { "1", LONG, 1L } };
	}

	/**
	 * Valid conversion cases for ENUM to:
	 * STRING, TEXT, INT, LONG
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(String valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "2b", INT, MolgenisValidationException.class,
				"Value [2b] of this entity attribute is not of type [INT or LONG]." },
				{ "2b", LONG, MolgenisValidationException.class,
						"Value [2b] of this entity attribute is not of type [INT or LONG]." },
				{ "1", DECIMAL, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [DECIMAL] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", XREF, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [XREF] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", CATEGORICAL, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [CATEGORICAL] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", DATE, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [DATE] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", DATE_TIME, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [DATE_TIME] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", MREF, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [MREF] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [CATEGORICAL_MREF] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [EMAIL] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", HTML, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [HTML] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [HYPERLINK] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", COMPOUND, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [COMPOUND] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", FILE, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [FILE] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", BOOL, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [BOOL] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1b", STRING, MolgenisValidationException.class,
						"Invalid [enum] value [1b] for attribute [mainAttribute] of entity [null] with type [MAINENTITY]. Value must be one of [1, 2b, abc]" },
				{ "1", SCRIPT, MolgenisValidationException.class,
						"Attribute data type update from [ENUM] to [SCRIPT] not allowed, allowed types are [INT, LONG, STRING, TEXT]" },
				{ "1", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversions cases for ENUM to:
	 * INT, LONG, DECIMAL, XREF, CATEGORICAL, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, EMAIL, HTML, HYPERLINK, COMPOUND, FILE, BOOL, STRING, SCRIPT, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(String valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
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
