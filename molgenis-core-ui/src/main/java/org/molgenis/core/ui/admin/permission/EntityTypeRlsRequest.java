package org.molgenis.core.ui.admin.permission;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

class EntityTypeRlsRequest
{
	@NotNull
	private final String id;
	private final boolean rlsEnabled;

	EntityTypeRlsRequest(String id, boolean rlsEnabled)
	{
		this.id = requireNonNull(id);
		this.rlsEnabled = rlsEnabled;
	}

	public String getId()
	{
		return id;
	}

	public boolean isRlsEnabled()
	{
		return rlsEnabled;
	}
}
