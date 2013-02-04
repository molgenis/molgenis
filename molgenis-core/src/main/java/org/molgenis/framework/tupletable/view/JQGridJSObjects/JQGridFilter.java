package org.molgenis.framework.tupletable.view.JQGridJSObjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//{"groupOp":"AND","rules":[{"field":"Country.Code","op":"eq","data":"AGO"}]}
public class JQGridFilter
{
	private String groupOp;
	private List<JQGridRule> rules;

	public JQGridFilter()
	{
		this.groupOp = "AND";
		this.rules = new ArrayList<JQGridRule>();
	}

	public String getGroupOp()
	{
		return groupOp;
	}

	public void setGroupOp(String groupOp)
	{
		this.groupOp = groupOp;
	}

	public List<JQGridRule> getRules()
	{
		return Collections.unmodifiableList(rules);
	}

	public void addRule(JQGridRule rule)
	{
		this.rules.add(rule);
	}
}
