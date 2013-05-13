package org.molgenis.mutationdb.plugins.testpieter;


import java.util.Arrays;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.EasyPluginController;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Entity;


public class TestPieter extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;
	
	public List<String> listOfNames = Arrays.asList("jan","piet","klaas","dennis");
	
	public String testString1 = "A dropdown menu:";
	public String testString2;
	
	public TestPieter(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return TestPieter.class.getSimpleName();
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/" + TestPieter.class.getName().replace('.', '/') + ".ftl";
	}


	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws Exception
	{
		
			// FIXME: This is a hack, fix ASAP
			EasyPluginController.HTML_WAS_ALREADY_SERVED = true;
		
	}


	@Override
	public void reload(Database db)
	{

	}
	

	public List<String> getListOfNames() {
		return listOfNames;
	}

	public String getTestString2() {
		return testString2;
	}

	public void setTestString2(String testString2) {
		this.testString2 = testString2;
	}

	public String getTestString1() {
		return testString1;
	}

}
