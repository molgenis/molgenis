package org.molgenis.app.promise;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class PromiseCredentialsMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "PromiseCredentials";
	public static final String FULLY_QUALIFIED_NAME = PromisePackage.NAME + '_' + ENTITY_NAME;

	public static final String ID = "ID";
	public static final String PROJ = "PROJ";
	public static final String USERNAME = "USERNAME";
	public static final String PASSW = "PASSW";
	public static final String PWS = "PWS";
	public static final String SECURITYCODE = "SECURITYCODE";
	public static final String URL = "URL";

	public static final EntityMetaData INSTANCE = new PromiseCredentialsMetaData();

	public PromiseCredentialsMetaData()
	{
		super(ENTITY_NAME, PromisePackage.getPackage());

		setLabel("ProMISe credentials");
		setDescription("Credentials for ProMISe SOAP endpoints");

		addAttribute(ID).setIdAttribute(true).setNillable(false);
		addAttribute(PROJ).setNillable(false);
		addAttribute(USERNAME).setNillable(false);
		addAttribute(PASSW).setNillable(false).setVisible(false);
		addAttribute(PWS).setNillable(false);
		addAttribute(SECURITYCODE).setNillable(false).setVisible(false);
		addAttribute(URL).setNillable(false);
	}
}
