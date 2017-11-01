package org.molgenis.security.core.model;

public enum ConceptualRoles
{
	GROUPADMIN("Group-administrator"), MANAGER("Manager"), EDITOR("Editor"), CURATOR("Curator"), CONTRIBUTOR("Contributor"), VIEWER("Viewer"), AGGREGATOR("Aggregator");

	private String description;

	ConceptualRoles(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
