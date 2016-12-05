package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL;
import static org.molgenis.data.meta.AttributeType.CATEGORICAL_MREF;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.EMAIL;
import static org.molgenis.data.meta.AttributeType.ENUM;
import static org.molgenis.data.meta.AttributeType.FILE;
import static org.molgenis.data.meta.AttributeType.HTML;
import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.meta.AttributeType.SCRIPT;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class HyperlinkAttributeTypeUpdateTest extends AbstractAttributeTypeUpdateTest
{
	@BeforeClass
	private void setup()
	{
		super.setup(HYPERLINK, STRING);
	}

	@AfterMethod
	private void afterMethod()
	{
		super.afterMethod(HYPERLINK);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { "https://www.google.com", STRING, "https://www.google.com" },
				{ "https://www.google.com", TEXT, "https://www.google.com" },
				{ "https://www.google.com", CATEGORICAL, "hyperlink label" },
				{ "https://www.google.com", XREF, "hyperlink label" } };
	}

	/**
	 * Valid conversion cases for HYPERLINK to:
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
		return new Object[][] { { "https://www.google.com", BOOL, MolgenisValidationException.class,
				"Attribute data type update from [HYPERLINK] to [BOOL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", INT, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [INT] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", LONG, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [LONG] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", DECIMAL, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [DECIMAL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "molgenis@test.nl", XREF, MolgenisValidationException.class,
						"Unknown xref value 'molgenis@test.nl' for attribute 'mainAttribute' of entity 'MAIN_ENTITY'." },
				{ "molgenis@test.nl", CATEGORICAL, MolgenisValidationException.class,
						"Unknown xref value 'molgenis@test.nl' for attribute 'mainAttribute' of entity 'MAIN_ENTITY'." },
				{ "https://www.google.com", SCRIPT, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [SCRIPT] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [EMAIL] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", HTML, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [HTML] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", ENUM, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [ENUM] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", DATE, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [DATE] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", DATE_TIME, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [DATE_TIME] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", MREF, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [MREF] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [CATEGORICAL_MREF] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", FILE, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [FILE] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", COMPOUND, MolgenisValidationException.class,
						"Attribute data type update from [HYPERLINK] to [COMPOUND] not allowed, allowed types are [CATEGORICAL, STRING, TEXT, XREF]" },
				{ "https://www.google.com", ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversions cases for HYPERLINK to:
	 * BOOL, INT, LONG, DECIMAL, XREF, CATEGORICAL, SCRIPT, EMAIL, HTML, ENUM, DATE, DATE_TIME, MREF, CATEGORICAL_MREF, FILE, COMPOUND, ONE_TO_MANY
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
