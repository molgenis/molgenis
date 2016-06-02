package org.molgenis.data.elasticsearch.logback;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class LoggingEventMetaData extends SystemEntityMetaDataImpl
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
		addAttribute(IDENTIFIER, ROLE_ID).setVisible(false);
		addAttribute(TIMESTAMP).setDataType(MolgenisFieldTypes.DATETIME);
		addAttribute(THREAD);
		addAttribute(LEVEL);
		addAttribute(LOGGER);
		addAttribute(MESSAGE).setDataType(MolgenisFieldTypes.TEXT);
		addAttribute(STACKTRACE).setDataType(MolgenisFieldTypes.TEXT);
	}
}
