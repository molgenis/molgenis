package org.molgenis.framework.server;

import javax.swing.JTextArea;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This logger appender makes it possible to log to a textarea.
 */
public class JTextAreaAppender extends AppenderSkeleton
{
	JTextArea jtext;

	public JTextAreaAppender(JTextArea jtext)
	{
		this.layout = new PatternLayout("%-7r %-5p %-15c %x - %m\n");
		this.jtext = jtext;
	}

	@Override
	protected void append(LoggingEvent event)
	{
		jtext.append(layout.format(event));

		if (layout.ignoresThrowable())
		{
			String[] s = event.getThrowableStrRep();
			if (s != null)
			{
				int len = s.length;
				for (int i = 0; i < len; i++)
				{
					jtext.append(s[i]);
					jtext.append(Layout.LINE_SEP);
				}
			}
		}
		jtext.setCaretPosition(jtext.getText().length());
	}

	@Override
	public boolean requiresLayout()
	{
		return true;
	}

	@Override
	public void close()
	{
		if (closed) return;
		closed = true;
		if (layout != null)
		{
			jtext.append(layout.getFooter());
		}
	}
}