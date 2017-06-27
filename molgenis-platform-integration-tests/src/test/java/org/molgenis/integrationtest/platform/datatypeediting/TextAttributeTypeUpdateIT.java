package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.text.ParseException;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class TextAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	@BeforeClass
	public void setUp()
	{
		super.setup(TEXT, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(TEXT);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionTestCases")
	public Object[][] validConversionTestCases()
	{
		return new Object[][] { { "true", BOOL, true }, { "1", INT, 1 }, { "4243298", LONG, 4243298L },
				{ "1.234", DECIMAL, 1.234 },
				{ "A VERY LONG TEXT!!!!@#$#{@}{@}{#%$#*($&@#", STRING, "A VERY LONG TEXT!!!!@#$#{@}{@}{#%$#*($&@#" },
				{ "1", ENUM, "1" }, { "<h1>Hello World</h1>", HTML, "<h1>Hello World</h1>" },
				{ "Compounds go!", COMPOUND, null }, { "1990-11-13", DATE, "1990-11-13" },
				{ "2016-11-13T20:20:20+0100", DATE_TIME, "2016-11-13T20:20:20+0100" } };
	}

	/**
	 * Valid conversion cases for TEXT to:
	 * INT, STRING, BOOL, DECIMAL, LONG, XREF, CATEGORICAL, COMPOUND, ENUM, HTML, DATE, DATE_TIME
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 * @throws ParseException
	 */
	@Test(dataProvider = "validConversionTestCases")
	public void testValidConversion(String valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
			throws ParseException
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		if (typeToConvertTo.equals(DATE)) convertedValue = parseLocalDate(convertedValue.toString());
		if (typeToConvertTo.equals(DATE_TIME)) convertedValue = parseInstant(convertedValue.toString());

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "not true", BOOL, MolgenisValidationException.class,
				"Value [not true] of this entity attribute is not of type [BOOL]." },
				{ "1b", INT, MolgenisValidationException.class,
						"Value [1b] of this entity attribute is not of type [INT or LONG]." },
				{ "1234567890b", LONG, MolgenisValidationException.class,
						"Value [1234567890b] of this entity attribute is not of type [INT or LONG]." },
				{ "1.123b", DECIMAL, MolgenisValidationException.class,
						"Value [1.123b] of this entity attribute is not of type [DECIMAL]." },
				{ "ref123", XREF, MolgenisValidationException.class,
						"Attribute data type update from [TEXT] to [XREF] not allowed, allowed types are [BOOL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, STRING]" },
				{ "ref123", CATEGORICAL, MolgenisValidationException.class,
						"Attribute data type update from [TEXT] to [CATEGORICAL] not allowed, allowed types are [BOOL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, STRING]" },
				{ "Test@Test.Test", EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [TEXT] to [EMAIL] not allowed, allowed types are [BOOL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, STRING]" },
				{ "https://www.google.com", HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [TEXT] to [HYPERLINK] not allowed, allowed types are [BOOL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, STRING]" },
				{ "enumOption100", ENUM, MolgenisValidationException.class,
						"Unknown enum value for attribute 'mainAttribute' of entity 'MAINENTITY'." },
				{ "Not a date", DATE, MolgenisValidationException.class,
						"Value [Not a date] of this entity attribute is not of type [DATE]." },
				{ "Not a date time", DATE_TIME, MolgenisValidationException.class,
						"Value [Not a date time] of this entity attribute is not of type [DATE_TIME]." },
				{ "ref123", MREF, MolgenisValidationException.class,
						"Attribute data type update from [TEXT] to [MREF] not allowed, allowed types are [BOOL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, STRING]" },
				{ "ref123", CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [TEXT] to [CATEGORICAL_MREF] not allowed, allowed types are [BOOL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, STRING]" },
				{ "ref123", FILE, MolgenisValidationException.class,
						"Attribute data type update from [TEXT] to [FILE] not allowed, allowed types are [BOOL, COMPOUND, DATE, DATE_TIME, DECIMAL, ENUM, HTML, INT, LONG, SCRIPT, STRING]" },
				{ "ref123", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for TEXT to:
	 * STRING, BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, EMAIL, HYPERLINK, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversions(String valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
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
