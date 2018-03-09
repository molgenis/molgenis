package org.molgenis.core.ui.admin.permission;

import static java.util.Objects.requireNonNull;

public class EntityTypeRlsResponse
{
	private final String id;
	private final String label;
	private final boolean rlsEnabled;
	private final boolean readonly;

	public EntityTypeRlsResponse(String id, String label, boolean rlsEnabled, boolean readonly)
	{
		this.id = requireNonNull(id);
		this.label = requireNonNull(label);
		this.rlsEnabled = rlsEnabled;
		this.readonly = readonly;
	}

	public String getId()
	{
		return id;
	}

	public String getLabel()
	{
		return label;
	}

	public boolean isRlsEnabled()
	{
		return rlsEnabled;
	}

	public boolean isReadonly()
	{
		return readonly;
	}

}
