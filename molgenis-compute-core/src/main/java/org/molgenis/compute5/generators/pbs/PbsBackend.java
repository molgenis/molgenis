package org.molgenis.compute5.generators.pbs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.molgenis.compute5.ComputeProperties;
import org.molgenis.compute5.generators.BackendGenerator;

public class PbsBackend extends BackendGenerator
{
	public PbsBackend() throws IOException
	{
		super("header.ftl","footer.ftl","submit.ftl");
	}
	
	public PbsBackend(ComputeProperties cp) throws IOException
	{
		super("header.ftl", "footer.ftl", "submit.ftl");

		File h = new File(cp.customHeader);
		File f = new File(cp.customFooter);
		File s = new File(cp.customSubmit);

		// overwrite if files already defined by user
		if (h.exists()) {
			System.out.println(">> Custom header: " + h);
			this.appendCustomHeader(FileUtils.readFileToString(h));
		} else
			System.out.println(">> Custom header not found (" + h + ")");

		if (f.exists()) {
			System.out.println(">> Custom footer: " + f);
			this.appendCustomFooter(FileUtils.readFileToString(f));
		} else
			System.out.println(">> Custom footer not found (" + f + ")");

		if (s.exists()) {
			System.out.println(">> Custom submit script: " + s);
			this.setSubmitTemplate(FileUtils.readFileToString(s));
		} else
			System.out.println(">> Custom submit script not found (" + s + ")");
	}

}
