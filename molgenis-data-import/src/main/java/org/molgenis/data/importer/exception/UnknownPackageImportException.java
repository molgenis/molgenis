package org.molgenis.data.importer.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownPackageImportException extends ImporterException
{
	private static final String ERROR_CODE = "I01";

	private final String entityId;
	private final String packageId;

	public UnknownPackageImportException(String packageId, String entityId)
	{
		super(ERROR_CODE);
		this.entityId = entityId;
		this.packageId = packageId;
	}

	public String getEntityId()
	{
		return entityId;
	}

	public String getPackageId()
	{
		return packageId;
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s entityId: %s", packageId.toString(), entityId.toString());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { packageId, entityId };
	}
}
