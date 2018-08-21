package org.molgenis.data.decorator;

import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.decorator.meta.DecoratorParameters;
import org.molgenis.validation.JsonValidator;

import java.util.stream.Stream;

/**
 * Triggers JSON validation for the 'parameters' field of a {@link DecoratorParameters) entity.
 */
public class DecoratorParametersRepositoryDecorator extends AbstractRepositoryDecorator<DecoratorParameters>
{
	private final JsonValidator jsonValidator;

	DecoratorParametersRepositoryDecorator(Repository<DecoratorParameters> delegateRepository,
			JsonValidator jsonValidator)
	{
		super(delegateRepository);
		this.jsonValidator = jsonValidator;
	}

	@Override
	public void update(DecoratorParameters decoratorParameters)
	{
		validateParameters(decoratorParameters);
		delegate().update(decoratorParameters);
	}

	@Override
	public void update(Stream<DecoratorParameters> decoratorParameters)
	{
		delegate().update(decoratorParameters.filter(job ->
		{
			validateParameters(job);
			return true;
		}));
	}

	@Override
	public void add(DecoratorParameters decoratorParameters)
	{
		validateParameters(decoratorParameters);
		delegate().add(decoratorParameters);
	}

	@Override
	public Integer add(Stream<DecoratorParameters> jobs)
	{
		return delegate().add(jobs.filter(job ->
		{
			validateParameters(job);
			return true;
		}));
	}

	private void validateParameters(DecoratorParameters decoratorParameters)
	{
		String schema = decoratorParameters.getDecorator().getSchema();
		if (schema != null)
		{
			jsonValidator.validate(decoratorParameters.getParameters(), schema);
		}
	}
}