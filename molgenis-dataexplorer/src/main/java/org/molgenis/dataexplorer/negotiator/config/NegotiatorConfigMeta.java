package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;

@Component
class NegotiatorConfigMeta extends SystemEntityType
{
	private static final String SIMPLE_NAME = "NegotiatorConfig";

	public static final String NEGOTIATOR_URL = "negotiator_url";
	public static final String USERNAME = "username";
	@java.lang.SuppressWarnings("squid:S2068")
	public static final String PASSWORD = "password";
	private static final String ID = "id";

	private final NegotiatorPackage negotiatorPackage;

	public NegotiatorConfigMeta(NegotiatorPackage negotiatorPackage)
	{
		super(SIMPLE_NAME, NegotiatorPackage.PACKAGE_NEGOTIATOR);
		this.negotiatorPackage = requireNonNull(negotiatorPackage);
	}

	@Override
	protected void init()
	{
		setLabel("Negotiator Config");
		setPackage(negotiatorPackage);
		addAttribute(ID, ROLE_ID).setDataType(AttributeType.STRING)
								 .setNillable(false)
								 .setLabel("Identifier for this config");
		addAttribute(NEGOTIATOR_URL).setLabel("Negotiator URL").setNillable(false);
		addAttribute(USERNAME).setDataType(AttributeType.STRING)
							  .setNillable(false)
							  .setLabel("Username for the negotiator");
		addAttribute(PASSWORD).setDataType(AttributeType.STRING)
							  .setNillable(false)
							  .setLabel("Password for the negotiator");
	}
}
