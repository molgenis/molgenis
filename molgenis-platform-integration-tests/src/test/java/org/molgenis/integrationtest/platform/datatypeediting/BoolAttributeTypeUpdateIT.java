package org.molgenis.integrationtest.platform.datatypeediting;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.integrationtest.platform.PlatformITConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.*;

import static org.molgenis.data.meta.AttributeType.*;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { PlatformITConfig.class })
@TestExecutionListeners(listeners = { WithSecurityContextTestExecutionListener.class })
@Transactional
public class BoolAttributeTypeUpdateIT extends AbstractAttributeTypeUpdateIT
{
	private static final String USERNAME = "bool-attribute-type-update-user";

	@BeforeClass
	public void setUp()
	{
		super.setup(BOOL, STRING);
	}

	@AfterMethod
	public void afterMethod()
	{
		super.afterMethod(BOOL);
	}

	@AfterClass
	public void afterClass()
	{
		super.afterClass();
	}

	@DataProvider(name = "validConversionData")
	public Object[][] validConversionData()
	{
		return new Object[][] { { true, STRING, "true" }, { false, STRING, "false" }, { true, TEXT, "true" },
				{ false, TEXT, "false" }, { true, INT, 1 }, { false, INT, 0 } };
	}

	/**
	 * Valid conversion cases for BOOLEAN to:
	 * STRING, TEXT, INT
	 *
	 * @param valueToConvert  The value that will be converted
	 * @param typeToConvertTo The type to convert to
	 * @param convertedValue  The expected value after converting the type
	 */
	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "validConversionData")
	public void testValidConversion(boolean valueToConvert, AttributeType typeToConvertTo, Object convertedValue)
	{
		testTypeConversion(valueToConvert, typeToConvertTo);

		// Assert if conversion was successful
		assertEquals(getActualDataType(), typeToConvertTo);
		assertEquals(getActualValue(), convertedValue);
	}

	@DataProvider(name = "invalidConversionTestCases")
	public Object[][] invalidConversionTestCases()
	{
		return new Object[][] { { true, DECIMAL, MolgenisValidationException.class,
				"Attribute data type update from [BOOL] to [DECIMAL] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, LONG, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [LONG] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, MREF, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [MREF] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, XREF, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [XREF] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, CATEGORICAL, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [CATEGORICAL] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, CATEGORICAL_MREF, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [CATEGORICAL_MREF] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, FILE, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [FILE] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, COMPOUND, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [COMPOUND] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, EMAIL, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [EMAIL] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, HTML, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [HTML] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, HYPERLINK, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [HYPERLINK] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, DATE, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [DATE] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, DATE_TIME, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [DATE_TIME] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, ENUM, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [ENUM] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, SCRIPT, MolgenisValidationException.class,
						"Attribute data type update from [BOOL] to [SCRIPT] not allowed, allowed types are [INT, STRING, TEXT]" },
				{ true, ONE_TO_MANY, MolgenisValidationException.class,
						"Invalid [xref] value [] for attribute [Referenced entity] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('refEntityType').isNull().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/).not()).or($('refEntityType').isNull().not().and($('type').matches(/^(categorical|categoricalmref|file|mref|onetomany|xref)$/))).value().Invalid [xref] value [] for attribute [Mapped by] of entity [mainAttribute] with type [sys_md_Attribute]. Offended validation expression: $('mappedBy').isNull().and($('type').eq('onetomany').not()).or($('mappedBy').isNull().not().and($('type').eq('onetomany'))).value()" } };
	}

	/**
	 * Invalid conversion cases for BOOL to:
	 * DECIMAL, LONG, MREF, XREF, CATEGORICAL, CATEGORICAL_MREF, FILE, COMPOUND, EMAIL, HTML, HYPERLINK, DATE, DATE_TIME, ENUM, ONE_TO_MANY, SCRIPT
	 *
	 * @param valueToConvert   The value that will be converted
	 * @param typeToConvertTo  The type to convert to
	 * @param exceptionClass   The expected class of the exception that will be thrown
	 * @param exceptionMessage The expected exception message
	 */
	@WithMockUser(username = USERNAME)
	@Test(dataProvider = "invalidConversionTestCases")
	public void testInvalidConversion(boolean valueToConvert, AttributeType typeToConvertTo, Class exceptionClass,
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
