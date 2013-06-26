package org.molgenis.omx.harmonizationIndexer.plugin;

import java.io.File;

import org.molgenis.framework.tupletable.TableException;

public interface HarmonizationIndexer
{
	void index(File file) throws TableException;
}
