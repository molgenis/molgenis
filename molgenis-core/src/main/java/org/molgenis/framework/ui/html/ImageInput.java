package org.molgenis.framework.ui.html;

/**
 * Input for showing images. Is extension of the FileInput with the difference
 * that the file value is not a clickable link but a rendered image.
 */
public class ImageInput extends FileInput
{
	private int width = 128;
	private int height = 128;

	public ImageInput(String name, Object value)
	{
		super(name, value);
	}

	/**
	 * Adapted to show thumbnail image instead of download button
	 */
	@Override
	public String getValue()
	{
		if (!super.getValue().isEmpty()) return getObject() + "<img src=\"" + super.getObject()
				+ "\" onclick=\"this.form.__filename.value = '" + super.getValue() + "';this.form.__action.value='"
				+ ACTION_DOWNLOAD + "'; return true;\"/>";
		return super.getValue();
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

}
