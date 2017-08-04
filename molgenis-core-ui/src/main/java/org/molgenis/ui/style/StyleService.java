package org.molgenis.ui.style;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public interface StyleService
{
	/**
	 * Fetches the available styleSheet from the data store and stores them in a list
	 *
	 * @return A Set of available Styles found on the file system
	 */
	Set<Style> getAvailableStyles();

	/**
	 * Add a bootstrap theme
	 *
	 * @param styleId             the them identifier used in the request
	 * @param bootstrap3FileName  the name to use as fileName for the bootstrap 3 file
	 * @param bootstrap3StyleData the bootstrap 3 style data
	 * @param bootstrap4FileName  the name to use as fileName for the bootstrap 4 file, this is optional
	 * @param bootstrap4StyleData the bootstrap 4 style data, this is optional
	 * @throws MolgenisStyleException
	 */
	void addStyles(String styleId, String bootstrap3FileName, InputStream bootstrap3StyleData,
			String bootstrap4FileName, InputStream bootstrap4StyleData) throws MolgenisStyleException;

	/**
	 * Set the runtime property that controls the css style with a selected style
	 */
	void setSelectedStyle(String styleName);

	/**
	 * Get the style that is currently selected
	 *
	 * @return Which style is currently selected
	 */
	Style getSelectedStyle();

	/**
	 * Returns a style that matches the name of the given input
	 *
	 * @return The Style that matches the name
	 */
	Style getStyle(String styleName);

	FileSystemResource getThemeData(String styleName, BootstrapVersion bootstrapVersion)
			throws MolgenisStyleException, IOException;
}
