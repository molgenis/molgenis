package org.molgenis.compute5.db.api;

import java.util.Collections;
import java.util.List;

public class GetBackendsResponse extends ApiResponse
{
	private List<Backend> backends;

	public void setBackends(List<Backend> backends)
	{
		this.backends = backends;
	}

	public List<Backend> getBackends()
	{
		return Collections.unmodifiableList(backends);
	}

}
