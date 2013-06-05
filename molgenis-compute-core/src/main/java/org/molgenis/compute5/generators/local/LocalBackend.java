package org.molgenis.compute5.generators.local;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.generators.BackendGenerator;

public class LocalBackend extends BackendGenerator
{

	public LocalBackend() throws IOException
	{
		super("header.ftl", "footer.ftl", "submit.ftl");
	}

	public LocalBackend(ComputeProperties cp) throws IOException
	{
		super("header.ftl", "footer.ftl", "submit.ftl");

		File h = new File(cp.path + File.separator + "header.ftl");
		File f = new File(cp.path + File.separator + "Footer.ftl");
		File s = new File(cp.path + File.separator + "Submit.ftl");

		// overwrite if files already defined by user
		if (h.exists()) this.setHeaderTemplate(FileUtils.readFileToString(h));
		if (f.exists()) this.setFooterTemplate(FileUtils.readFileToString(f));
		if (s.exists()) this.setSubmitTemplate(FileUtils.readFileToString(s));
	}

}
