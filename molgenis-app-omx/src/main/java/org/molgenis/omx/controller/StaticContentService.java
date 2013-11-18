package org.molgenis.omx.controller;

import org.springframework.ui.Model;

/**
 * Controller that handles static content pages requests
 * 
 * RuntimeProperty_[KeyApp] is the way an identifier is made
 */

public interface StaticContentService
{
	String init(final String uniqueReference, final Model model);

	String initEdit(final String uniqueReference, final Model model);

	boolean submitContent(final String uniqueReference, final String content);
}
