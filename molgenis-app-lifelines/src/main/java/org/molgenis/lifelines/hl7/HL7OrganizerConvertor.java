package org.molgenis.lifelines.hl7;

import org.molgenis.omx.observ.Protocol;
import org.molgenis.hl7.CD;
import org.molgenis.hl7.REPCMT000100UV01Organizer;

public class HL7OrganizerConvertor
{
	private HL7OrganizerConvertor()
	{
	}

	public static Protocol toProtocol(REPCMT000100UV01Organizer organizer)
	{
		CD code = organizer.getCode();
		Protocol protocol = new Protocol();
		protocol.setIdentifier(code.getCodeSystem() + '.' + code.getCode());
		protocol.setName(code.getDisplayName());
		return protocol;
	}
}
