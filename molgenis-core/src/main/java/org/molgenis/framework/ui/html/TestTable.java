package org.molgenis.framework.ui.html;

public class TestTable
{
	public static void main(String[] args)
	{
		TableBeta t = new TableBeta("test");
		t.setClazz("molgenis_matrix");

		t.set(0, 0, new CustomHtml("value0.0"));
		t.set(0, 1, new CustomHtml("value0.1"), 1, 1);
		t.set(0, 0, new CustomHtml("value1.0"), 1, 1);
		t.set(0, 1, new CustomHtml("value1.1"), 1, 1);

		System.out.println(t.render());
	}
}
