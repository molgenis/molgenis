package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.EntityReferenceUnknownConstraintViolationException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class EmailAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(EMAIL, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(EMAIL);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "molgenis@test.org", STRING, "molgenis@test.org" },
				{ "molgenis@test.org", TEXT, "molgenis@test.org" }, { "molgenis@test.org", CATEGORICAL, "email label" },
				{ "molgenis@test.org", XREF, "email label" } };
	}

	/**
	 * Valid conversion cases for EMAIL to:
	 * STRING, TEXT, CATEGORICAL, XREF
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
		return new Object[][] { { "molgenis@test.org", BOOL,
				"Attribute data type update from [EMAIL] to [BOOL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", INT,
						"Attribute data type update from [EMAIL] to [INT] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", LONG,
						"Attribute data type update from [EMAIL] to [LONG] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", DECIMAL,
						"Attribute data type update from [EMAIL] to [DECIMAL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.nl", XREF, EntityReferenceUnknownConstraintViolationException.class,
						"type:MAINENTITY attribute:mainAttribute value: molgenis@test.nl" },
				{ "molgenis@test.nl", CATEGORICAL, EntityReferenceUnknownConstraintViolationException.class,
						"type:MAINENTITY attribute:mainAttribute value: molgenis@test.nl" },
				{ "molgenis@test.org", SCRIPT,
						"Attribute data type update from [EMAIL] to [SCRIPT] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", HYPERLINK,
						"Attribute data type update from [EMAIL] to [HYPERLINK] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", HTML,
						"Attribute data type update from [EMAIL] to [HTML] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", ENUM,
						"Attribute data type update from [EMAIL] to [ENUM] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", DATE,
						"Attribute data type update from [EMAIL] to [DATE] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", DATE_TIME,
						"Attribute data type update from [EMAIL] to [DATE_TIME] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", MREF,
						"Attribute data type update from [EMAIL] to [MREF] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", CATEGORICAL_MREF,
						"Attribute data type update from [EMAIL] to [CATEGORICAL_MREF] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", FILE,
						"Attribute data type update from [EMAIL] to [FILE] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", COMPOUND,
						"Attribute data type update from [EMAIL] to [COMPOUND] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.org", ONE_TO_MANY,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversions cases for EMAIL to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, HYPERLINK, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(String valueToConvert, AttributeType typeToConvertTo, String exceptionMessage)
	{
		try
		{
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (ValidationException e)
		{
			assertEquals(e.getMessage(), exceptionMessage);
		}
	}
}
