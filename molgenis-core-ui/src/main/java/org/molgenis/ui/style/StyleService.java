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
	 * Add given the style files to style folder
	 * @param bootstrap3Style this is monitory and may not be null
	 * @param bootstrap4Style this is optional and may be set to null to indicate not bootstrap4 style needs to be added
	 */
	void addStyles(MultipartFile bootstrap3Style, MultipartFile bootstrap4Style) throws MolgenisStyleException;

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
