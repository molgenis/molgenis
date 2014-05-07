package org.molgenis.ui.controller;


/**
 * Controller that handles static content pages requests
 */

public interface StaticContentService
{
	String getContent(String uniqueReference);

	boolean submitContent(final String uniqueReference, final String content);

	boolean isCurrentUserCanEdit();
}
