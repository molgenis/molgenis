package org.molgenis.data;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileBasedEntitySourceFactory extends EntitySourceFactory
{
	List<String> getFileExtensions();

	EntitySource create(File file) throws IOException;
}
