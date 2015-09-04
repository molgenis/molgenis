package org.molgenis.data.annotation.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by charbonb on 24/08/15.
 */
public interface JarRunner
{

	public File runJar(String outputFileName, List<String> params, File input) throws IOException, InterruptedException;
}
