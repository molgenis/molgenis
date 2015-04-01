package org.molgenis.security.runas;

@FunctionalInterface
public interface RunnableAsSystem<T, X extends Throwable>
{
	T run() throws X;
}