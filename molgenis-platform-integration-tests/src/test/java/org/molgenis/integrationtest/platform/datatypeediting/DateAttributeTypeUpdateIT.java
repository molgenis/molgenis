package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.text.ParseException;
import java.time.LocalDate;

import static java.time.ZoneId.systemDefault;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class DateAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{

	@BeforeClass
	public void setup()
	{
		super.setup(DATE, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(DATE);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionTestCases")
	public Object[][] validConversionTestCases()
	{
		return new Object[][] { { LocalDate.parse("2016-11-13"), STRING, "2016-11-13" },
				{ LocalDate.parse("2016-11-13"), TEXT, "2016-11-13" }, { LocalDate.parse("2016-11-13"), DATE_TIME,
				LocalDate.parse("2016-11-13").atStartOfDay(systemDefault()).toInstant() } };
	}

	/**
	 * Valid conversion cases for DATE to:
	 * STRING, TEXT, DATE_TIME
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@Test(dataProvider = "validConversionTestCases")
	public void testValidConversion(LocalDate valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
			throws ParseException
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "2016-11-13", BOOL, MolgenisDataException.class,
				"Attribute data type update from [DATE] to [BOOL] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", INT, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [INT] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", LONG, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [LONG] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", DECIMAL, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [DECIMAL] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", XREF, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [XREF] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", CATEGORICAL, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [CATEGORICAL] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", SCRIPT, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [SCRIPT] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", HYPERLINK, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [HYPERLINK] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", EMAIL, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [EMAIL] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", ENUM, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [ENUM] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", HTML, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [HTML] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", MREF, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [MREF] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", CATEGORICAL_MREF, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [CATEGORICAL_MREF] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", FILE, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [FILE] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", COMPOUND, MolgenisDataException.class,
						"Attribute data type update from [DATE] to [COMPOUND] not allowed, allowed types are [DATE_TIME, STRING, TEXT]" },
				{ "2016-11-13", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for TEXT to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, HYPERLINK, EMAIL, ENUM, HTML, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversions(Object valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
			String exceptionMessage) throws ParseException
	{
		try
		{
			valueToConvert = parseLocalDate(valueToConvert.toString());
			testTypeConversion(valueToConvert, typeToConvertTo);
			fail("Conversion should have failed");
		}
		catch (Exception exception)
		{
			System.out.println(exception.getClass());
			System.out.println(exception.getMessage());
			assertTrue(exception.getClass().isAssignableFrom(exceptionClass));
			assertEquals(exception.getMessage(), exceptionMessage);
		}
	}
}
