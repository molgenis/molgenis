package org.molgenis.util;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.testng.annotations.Test;

public class DependencyResolverTest
{

	@Test
	public void resolve()
	{
		DefaultEntityMetaData e1 = new DefaultEntityMetaData("e1");
		DefaultEntityMetaData e2 = new DefaultEntityMetaData("e2");
		DefaultEntityMetaData e3 = new DefaultEntityMetaData("e3");
		DefaultEntityMetaData e4 = new DefaultEntityMetaData("e4");
		DefaultEntityMetaData e5 = new DefaultEntityMetaData("e5");

		e1.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e5);
		e5.setExtends(e3);
		e3.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e4);
		e3.addAttribute("refSelf").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e3);
		e4.addAttribute("ref").setDataType(MolgenisFieldTypes.XREF).setRefEntity(e2);

		List<EntityMetaData> resolved = DependencyResolver.resolve(Arrays.<EntityMetaData> asList(e1, e2, e3, e4, e5));
		assertEquals(resolved, Arrays.asList(e2, e4, e3, e5, e1));
	}
}
