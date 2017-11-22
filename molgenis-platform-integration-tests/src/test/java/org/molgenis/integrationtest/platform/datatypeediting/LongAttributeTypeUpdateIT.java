package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class LongAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(LONG, LONG);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(LONG);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { 1L, STRING, "1" }, { 1L, TEXT, "1" }, { 1L, INT, 1 }, { 1L, DECIMAL, 1.0 },
				{ 1L, ENUM, "1" }, { 1L, XREF, "label1" }, { 1L, CATEGORICAL, "label1" } };
	}

	/**
	 * Valid conversion cases for LONG to:
	 * STRING, TEXT, INT, DECIMAL, ENUM, XREF, CATEGORICAL
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(long valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { 2L, XREF, "type:MAINENTITY attribute:mainAttribute value: 2" },
				{ 2L, CATEGORICAL, "type:MAINENTITY attribute:mainAttribute value: 2" },
				{ 4L, ENUM, "type:MAINENTITY" }, { 1L, DATE,
				"Attribute data type update from [LONG] to [DATE] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, DATE_TIME,
						"Attribute data type update from [LONG] to [DATE_TIME] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, MREF,
						"Attribute data type update from [LONG] to [MREF] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, CATEGORICAL_MREF,
						"Attribute data type update from [LONG] to [CATEGORICAL_MREF] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, EMAIL,
						"Attribute data type update from [LONG] to [EMAIL] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, HTML,
						"Attribute data type update from [LONG] to [HTML] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, HYPERLINK,
						"Attribute data type update from [LONG] to [HYPERLINK] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, COMPOUND,
						"Attribute data type update from [LONG] to [COMPOUND] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, FILE,
						"Attribute data type update from [LONG] to [FILE] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, BOOL,
						"Attribute data type update from [LONG] to [BOOL] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, SCRIPT,
						"Attribute data type update from [LONG] to [SCRIPT] not allowed, allowed types are [CATEGORICAL, DECIMAL, ENUM, INT, STRING, TEXT, XREF]" },
				{ 1L, ONE_TO_MANY,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversions cases for LONG to:
	 * XREF, CATEGORICAL, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, EMAIL, HTML, HYPERLINK, COMPOUND, FILE, BOOL, SCRIPT, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(long valueToConvert, AttributeType typeToConvertTo, String exceptionMessage)
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
