<#include "GeneratorHelper.ftl">
package ${package};

import javax.servlet.ServletException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.framework.server.services.MolgenisSoapService;

public class SoapService extends MolgenisSoapService implements MolgenisService
{
	private static final long serialVersionUID = 1L;

	public SoapService(MolgenisContext mc) throws ServletException
	{
		super(mc);
	}
	
	@Override
	public Object getSoapImpl()
	{
		return new app.servlet.SoapApi(freshDatabase);
	}
}
