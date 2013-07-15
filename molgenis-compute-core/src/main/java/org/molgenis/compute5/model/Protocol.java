package org.molgenis.compute5.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Description of a protocol, inputs, outputs and script template. */
public class Protocol
{
	// reserved parameter names, used form system purposes
	public static List<String> reservedNames = Arrays.asList(new String[]
	{
			Parameters.PORT_CMNDLINE_OPTION,
			Parameters.INTERVAL_CMNDLINE_OPTION,
			Parameters.PATH, Parameters.WORKFLOW,
			Parameters.DEFAULTS,
			Parameters.PARAMETERS,
			Parameters.RUNDIR,
			Parameters.RUNID,
			Parameters.BACKEND,
			Parameters.DATABASE,
			Parameters.WALLTIME,
			Parameters.NODES,
			Parameters.PPN,
			Parameters.QUEUE,
			Parameters.MEMORY,
			Parameters.NOTAVAILABLE,
			Parameters.MOLGENIS_PASS_CMNDLINE,
			Parameters.MOLGENIS_USER_CMNDLINE,
			Parameters.CUSTOM_HEADER_COLUMN,
			Parameters.CUSTOM_FOOTER_COLUMN,
			Parameters.CUSTOM_SUBMIT_COLUMN
	});

	// unique name of the protocol
	private String name = null;

	// optional description of the protocol
	private String description;

	// resources
	private String walltime = null;
	private String defaultWalltime = "08:00:00";// walltime for protocol
	private String nodes = null;
	private String defaultNodes	= "1";
	private String ppn = null;
	private String defaultPpn = "4";
	private String queue = null;
	private String defaultQueue = "default";
	private String defaultMemory = "1Gb";
	private String memory = null;

	// list of inputs it expects from user_* or previousStep_*
	private Set<Input> inputs = new HashSet<Input>();

	// outputs that this protocol produces
	private Set<Output> outputs = new HashSet<Output>();

	// freemarker template of the protocol
	private String template = null;

	public Protocol(String name)
	{
		this.name = name;
	}

	// getters/setters below

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTemplate()
	{
		return template;
	}

	public void setTemplate(String template)
	{
		this.template = template;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public Set<Input> getInputs()
	{
		return inputs;
	}

	public void setInputs(Set<Input> inputs)
	{
		this.inputs = inputs;
	}

	public Set<Output> getOutputs()
	{
		return outputs;
	}

	public void setOutputs(Set<Output> outputs)
	{
		this.outputs = outputs;
	}

	public String getNodes()
	{
		return nodes;
	}

	public void setNodes(String nodes)
	{
		this.nodes = nodes;
	}
	
	public String getWalltime()
	{
		return this.walltime;
	}
	
	public void setWalltime(String walltime)
	{
		this.walltime = walltime;
	}

	public String getPpn()
	{
		return ppn;
	}

	public void setPpn(String ppn)
	{
		this.ppn = ppn;
	}

	public String getQueue()
	{
		return queue;
	}

	public void setQueue(String queue)
	{
		this.queue = queue;
	}

	public String getMemory()
	{
		return memory;
	}

	public void setMemory(String memory)
	{
		this.memory = memory;
	}

	public String getDefaultWalltime()
	{
		return defaultWalltime;
	}

	public String getDefaultNodes()
	{
		return defaultNodes;
	}

	public String getDefaultPpn()
	{
		return defaultPpn;
	}

	public String getDefaultQueue()
	{
		return defaultQueue;
	}

	public String getDefaultMemory()
	{
		return defaultMemory;
	}
}
