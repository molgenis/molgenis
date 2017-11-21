package org.molgenis.data.importer.wizard;

import org.mockito.Mock;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.data.validation.ValidationMessage;
import org.molgenis.test.AbstractMockitoTest;
import org.slf4j.Logger;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class ImportWizardUtilTest extends AbstractMockitoTest
{
	@Mock
	private ImportWizard importWizard;
	@Mock
	private BindingResult bindingResult;
	@Mock
	private Logger logger;

	@Test
	public void testHandleException() throws Exception
	{
		Exception exception = mock(Exception.class);
		when(exception.getLocalizedMessage()).thenReturn("Localized message");
		when(logger.isWarnEnabled()).thenReturn(true);

		ImportWizardUtil.handleException(exception, importWizard, bindingResult, logger, "MyEntityImportOption");
		verify(logger).warn(eq("Import of file [UNKNOWN] failed for action [MyEntityImportOption]"),
				any(Exception.class));
		verify(bindingResult).addError(new ObjectError("wizard", "<b>Your import failed:</b><br />Localized message"));
	}

	@Test
	public void testHandleExceptionValidationException() throws Exception
	{
		ValidationException validationException = mock(ValidationException.class);
		when(validationException.getValidationMessages()).thenReturn(
				Stream.of(ValidationMessage.create("0", "message #0", "localized message #0"),
						ValidationMessage.create("1", "message #1", "localized message #1")));
		when(logger.isWarnEnabled()).thenReturn(true);

		ImportWizardUtil.handleException(validationException, importWizard, bindingResult, logger,
				"MyEntityImportOption");
		verify(logger).warn(eq("Import of file [UNKNOWN] failed for action [MyEntityImportOption]"),
				any(Exception.class));
		verify(bindingResult).addError(new ObjectError("wizard",
				"<b>Your import failed:</b><br />localized message #0<br />localized message #1"));
	}

	@Test
	public void testHandleExceptionWarnLoggerDisabled() throws Exception
	{
		Exception exception = mock(Exception.class);
		when(exception.getLocalizedMessage()).thenReturn("Localized message");
		when(logger.isWarnEnabled()).thenReturn(false);

		ImportWizardUtil.handleException(exception, importWizard, bindingResult, logger, "MyEntityImportOption");
		verify(logger, times(0)).warn(anyString(), any(Exception.class));
		verify(bindingResult).addError(new ObjectError("wizard", "<b>Your import failed:</b><br />Localized message"));
	}
}