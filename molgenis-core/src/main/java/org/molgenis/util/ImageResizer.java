package org.molgenis.util;

import java.io.File;

public interface ImageResizer
{
	/**
	 * Function to resize images
	 * 
	 * @param src
	 *            image
	 * @param dest
	 *            image
	 * @param destWidth
	 *            new size
	 * @param destHeight
	 *            new size
	 * @return true if success
	 * @throws Exception
	 */
	public boolean resize(File src, File dest, int destWidth, int destHeight) throws Exception;
}