package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.annotation.core.datastructures.Location;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class LocationMismatchException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN05";
	private Location location;
	private Location thisLoc;

	public LocationMismatchException(Location location, Location thisLoc)
	{
		super(ERROR_CODE);
		this.location = location;
		this.thisLoc = thisLoc;

	}

	@Override
	public String getMessage()
	{
		return String.format("type:%s id:%s", location, thisLoc);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, location, thisLoc);
		}).orElse(super.getLocalizedMessage());
	}
}
