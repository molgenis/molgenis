package org.molgenis.security.core;

public enum Permission
{
	READ, WRITE, /**
 * COUNT permission on an entity type:
 * <ul>
 * <li>means that entities can be counted and aggregated</li>
 * <li>the entity type can be <b>READ</b></li>
 * </ul>
 */
COUNT, NONE, WRITEMETA
}