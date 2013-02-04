package org.molgenis.framework.ui.html;

/**
 * Input is the base-class for all the input classes. It provides the common
 * interface as well as some convenience methods for processing the inputs.
 * 
 * An Input has:
 * <ul>
 * <li>Name: unique name within a form
 * <li>Id: a unique id of this input. FIXME remove? Name is also unique?
 * <li>Value: the object value of the data for this input
 * <li>Label: a pretty label to show for this input on screen
 * <li>Readonly: indicating whether this input can be edited
 * <li>Hidden: indicating whether this input is shown.
 * <li>Required: whether input is required for this field.
 * <li>Tooltip: a short title describing the input.
 * <li>Description: a short title describing the input. FIXME: what is the
 * difference with tooltip?
 * <li>Style: css sentence for this input. FIXME: is this still used?
 * </ul>
 */
public interface Input<E>
{

}