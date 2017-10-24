package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class HtmlAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(HTML, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(HTML);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "<h1>This is the best MOLGENIS test in the world</h1>", STRING,
				"<h1>This is the best MOLGENIS test in the world</h1>" },
				{ "<h1>This is the best MOLGENIS test in the world</h1>", TEXT,
						"<h1>This is the best MOLGENIS test in the world</h1>" },
				{ "<h1>This is the best MOLGENIS test in the world</h1>", SCRIPT,
						"<h1>This is the best MOLGENIS test in the world</h1>" } };
	}

	/**
	 * Valid conversion cases for HTML to:
	 * STRING, TEXT, SCRIPT
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
		return new Object[][] { { "<h1>can not compute</h1>", BOOL, MolgenisValidationException.class,
				"Attribute data type update from [HTML] to [BOOL] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", INT, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [INT] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", LONG, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [LONG] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", DECIMAL, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [DECIMAL] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", XREF, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [XREF] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", CATEGORICAL, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [CATEGORICAL] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [HYPERLINK] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [EMAIL] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", ENUM, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [ENUM] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", DATE, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [DATE] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", DATE_TIME, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [DATE_TIME] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", MREF, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [MREF] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [CATEGORICAL_MREF] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", FILE, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [FILE] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", COMPOUND, MolgenisValidationException.class,
						"Attribute data type update from [HTML] to [COMPOUND] not allowed, allowed types are [SCRIPT, STRING, TEXT]" },
				{ "<h1>can not compute</h1>", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversions cases for HTML to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, HYPERLINK, EMAIL, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
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
