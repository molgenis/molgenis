package org.molgenis.core.ui.admin.permission;

import static java.util.Objects.requireNonNull;

public class EntityTypeRlsResponse
{
	private final String id;
	private final String label;

	private final boolean rlsEnabled;

	public EntityTypeRlsResponse(String id, String label, boolean rlsEnabled)
	{
		this.id = requireNonNull(id);
		this.label = requireNonNull(label);
		this.rlsEnabled = rlsEnabled;
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

}
