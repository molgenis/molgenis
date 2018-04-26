package org.molgenis.util;

import java.util.Iterator;

/**
 * Iterator that closes a resource if the decorated iterator is exhausted.
 * <p>
 * If you stop iterating before the iterator is exhausted, you have to call close yourself
 *
 * @param <E>
 */
public interface CloseableIterator<E> extends Iterator<E>
{
	void close();
}
