package org.molgenis.security.core.runas;

@FunctionalInterface
public interface RunnableAsSystem<T, X extends Throwable>
{
	T run() throws X;
}