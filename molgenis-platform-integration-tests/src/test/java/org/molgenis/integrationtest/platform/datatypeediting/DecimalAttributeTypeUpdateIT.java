package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.EnumConstraintModificationException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class DecimalAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	private void setup()
	{
		super.setup(DECIMAL, INT);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(DECIMAL);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { 1.123, STRING, "1.123" }, { 1.123, TEXT, "1.123" }, { 1.123, INT, 1 },
				{ 1.123, LONG, 1L }, { 1.0, ENUM, "1" } };
	}

	/**
	 * Valid conversion cases for DECIMAL to:
	 * STRING, TEXT, INT, LONG, ENUM
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(double valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { 2L, XREF,
				"Attribute data type update from [DECIMAL] to [XREF] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 2.0, CATEGORICAL,
						"Attribute data type update from [DECIMAL] to [CATEGORICAL] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 4.0, ENUM, EnumConstraintModificationException.class, "type:MAINENTITY" }, { 1.0, DATE,
				"Attribute data type update from [DECIMAL] to [DATE] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, DATE_TIME,
						"Attribute data type update from [DECIMAL] to [DATE_TIME] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, MREF,
						"Attribute data type update from [DECIMAL] to [MREF] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, CATEGORICAL_MREF,
						"Attribute data type update from [DECIMAL] to [CATEGORICAL_MREF] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, EMAIL,
						"Attribute data type update from [DECIMAL] to [EMAIL] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, HTML,
						"Attribute data type update from [DECIMAL] to [HTML] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, HYPERLINK,
						"Attribute data type update from [DECIMAL] to [HYPERLINK] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, COMPOUND,
						"Attribute data type update from [DECIMAL] to [COMPOUND] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, FILE,
						"Attribute data type update from [DECIMAL] to [FILE] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, BOOL,
						"Attribute data type update from [DECIMAL] to [BOOL] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, SCRIPT,
						"Attribute data type update from [DECIMAL] to [SCRIPT] not allowed, allowed types are [ENUM, INT, LONG, STRING, TEXT]" },
				{ 1.0, ONE_TO_MANY,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversions cases for DECIMAL to:
	 * XREF, CATEGORICAL, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, EMAIL, HTML, HYPERLINK, COMPOUND, FILE, BOOL, SCRIPT, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(double valueToConvert, AttributeType typeToConvertTo, String exceptionMessage)
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
