package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class NegotiatorConfigMeta extends SystemEntityType
{
	public static final String SIMPLE_NAME = "NegotiatorConfig";

	public static final String NEGOTIATORENTITYCONFIG =
			NegotiatorPackage.PACKAGE_NEGOTIATOR + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NEGOTIATOR_URL = "negotiator_url";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String ID = "id";

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
		addAttribute(ID, ROLE_ID).setDataType(AttributeType.STRING).setNillable(false);
		addAttribute(NEGOTIATOR_URL).setLabel("Negotiator URL").setNillable(false);
		addAttribute(USERNAME).setDataType(AttributeType.STRING).setNillable(false);
		addAttribute(PASSWORD).setDataType(AttributeType.STRING).setNillable(false);
	}
}
