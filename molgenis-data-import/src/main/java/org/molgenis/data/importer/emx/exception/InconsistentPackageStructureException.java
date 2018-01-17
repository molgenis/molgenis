package org.molgenis.data.importer.emx.exception;

/**
 * Thrown if the name of a package does not start with the fully qualified name of its parent
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InconsistentPackageStructureException extends EmxException
{
	private static final String ERROR_CODE = "E01";
	private final String pack;
	private final String parent;

	public InconsistentPackageStructureException(String pack, String parent)
	{
		super(ERROR_CODE);
		this.pack = pack;
		this.parent = parent;
	}

	@Override
	public String getMessage()
	{
		return String.format("package:%s parent:%s", pack, parent);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { pack, parent };
	}
}
