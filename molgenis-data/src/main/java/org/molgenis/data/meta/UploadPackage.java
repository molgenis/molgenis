package org.molgenis.data.meta;

import org.molgenis.data.meta.model.PackageMetadata;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.DefaultPackage.PACKAGE_DEFAULT;

@Component
@SuppressWarnings("squid:S2160")
public class UploadPackage extends SystemPackage
{
	private static final String SIMPLE_NAME = "upload";
	public static final String UPLOAD = PACKAGE_DEFAULT + PACKAGE_SEPARATOR + SIMPLE_NAME;

	private final DefaultPackage defaultPackage;

	public UploadPackage(PackageMetadata packageMetadata, DefaultPackage defaultPackage)
	{
		super(UPLOAD, packageMetadata);
		this.defaultPackage = requireNonNull(defaultPackage);
	}

	@Override
	protected void init()
	{
		setParent(defaultPackage);
		setLabel("Upload");
		setDescription("Package to upload to for VCF and the one click importer");
	}
}