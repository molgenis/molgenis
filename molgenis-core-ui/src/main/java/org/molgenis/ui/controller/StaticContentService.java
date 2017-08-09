package org.molgenis.ui.controller;

/**
 * Controller that handles static content pages requests
 */

public interface StaticContentService
{
	/**
	 * Get static page content
	 *
	 * @param uniqueReference
	 * @return content or null if no content exists for this reference
	 */
	String getContent(String uniqueReference);

	boolean submitContent(final String uniqueReference, final String content);

	boolean isCurrentUserCanEdit(String pluginId);

	void checkPermissions(String pluginId);
}
