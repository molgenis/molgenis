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
	{ "cores" });

	// unique name of the protocol
	String name;

	// optional description of the protocol
	String description;

	// number of cores that this protocol needs
	int cores = 4;

	// list of inputs it expects from user.* or previousStep.*
	Set<Input> inputs = new HashSet<Input>();

	// outputs that this protocol produces
	Set<Output> outputs = new HashSet<Output>();

	// freemarker template of the protocol
	String template;

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

	public int getCores()
	{
		return cores;
	}

	public void setCores(int cores)
	{
		this.cores = cores;
	}

}
