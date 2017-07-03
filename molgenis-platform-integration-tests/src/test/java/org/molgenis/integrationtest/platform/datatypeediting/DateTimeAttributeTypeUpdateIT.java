package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.*;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.util.MolgenisDateFormat.parseLocalDate;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class DateTimeAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{

	@BeforeClass
	public void setup()
	{
		super.setup(DATE_TIME, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(DATE_TIME);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionTestCases")
	public Object[][] validConversionTestCases()
	{
		Instant value = Instant.parse("2016-11-13T19:20:20Z");
		String formattedInDefaultTimezone = value.atZone(ZoneId.systemDefault())
												 .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx"));
		return new Object[][] { { value, STRING, formattedInDefaultTimezone },
				{ value, TEXT, formattedInDefaultTimezone }, { value, DATE, "2016-11-13" } };
	}

	/**
	 * Valid conversion cases for DATE_TIME to:
	 * STRING, TEXT, DATE
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 * @throws ParseException
	 */
	@Test(dataProvider = "validConversionTestCases")
	public void testValidConversion(Instant valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
			throws ParseException
	{
		valueToConvert = parseInstant(valueToConvert.toString());
		testTypeConversion(valueToConvert, typeToConvertTo);

		if (typeToConvertTo.equals(DATE)) convertedValue = parseLocalDate(convertedValue.toString());

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { "2016-11-13T20:20:20+0100", BOOL, MolgenisValidationException.class,
				"Attribute data type update from [DATE_TIME] to [BOOL] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", INT, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [INT] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", LONG, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [LONG] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", DECIMAL, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [DECIMAL] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", XREF, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [XREF] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", CATEGORICAL, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [CATEGORICAL] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", SCRIPT, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [SCRIPT] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [HYPERLINK] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [EMAIL] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", ENUM, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [ENUM] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", HTML, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [HTML] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", MREF, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [MREF] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [CATEGORICAL_MREF] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", FILE, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [FILE] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", COMPOUND, MolgenisValidationException.class,
						"Attribute data type update from [DATE_TIME] to [COMPOUND] not allowed, allowed types are [DATE, STRING, TEXT]" },
				{ "2016-11-13T20:20:20+0100", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for DATE_TIME to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, HYPERLINK, EMAIL, ENUM, HTML, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	// FIXME @Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversions(Object valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
			String exceptionMessage) throws ParseException
	{
		try
		{
			valueToConvert = parseInstant(valueToConvert.toString());
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
