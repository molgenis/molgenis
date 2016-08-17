package org.molgenis.data.elasticsearch.logback;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.AttributeType.DATE_TIME;
import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class LoggingEventMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "LoggingEvent";
	public static final String LOGGING_EVENT = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String THREAD = "thread";
	public static final String LEVEL = "level";
	public static final String LOGGER = "logger";
	public static final String MESSAGE = "message";
	public static final String TIMESTAMP = "timestamp";
	public static final String STACKTRACE = "stacktrace";

	LoggingEventMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Logging event");
		addAttribute(IDENTIFIER, ROLE_ID).setVisible(false);
		addAttribute(TIMESTAMP).setDataType(DATE_TIME);
		addAttribute(THREAD);
		addAttribute(LEVEL);
		addAttribute(LOGGER);
		addAttribute(MESSAGE).setDataType(TEXT);
		addAttribute(STACKTRACE).setDataType(TEXT);
	}
}
