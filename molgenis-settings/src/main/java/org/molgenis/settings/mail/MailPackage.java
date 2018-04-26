package org.molgenis.settings.mail;

import org.molgenis.data.meta.SystemPackage;
import org.molgenis.data.meta.model.PackageMetadata;
import org.molgenis.data.system.model.RootSystemPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class MailPackage extends SystemPackage
{
	public static final String SIMPLE_NAME = "mail";
	public static final String PACKAGE_MAIL = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private RootSystemPackage rootSystemPackage;

	public MailPackage(PackageMetadata packageMetadata)
	{
		super(PACKAGE_MAIL, packageMetadata);
	}

	@Override
	protected void init()
	{
		setLabel("Mail");
		setDescription("Mail properties");
		setParent(rootSystemPackage);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setRootSystemPackage(RootSystemPackage rootSystemPackage)
	{
		this.rootSystemPackage = requireNonNull(rootSystemPackage);
	}
}
