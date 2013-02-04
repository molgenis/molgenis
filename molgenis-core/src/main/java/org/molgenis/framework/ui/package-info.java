/**
 * {@link org.molgenis.framework.ui.ScreenModel} framework for showing a graphical {@link org.molgenis.framework.ui.ApplicationController} to the user.
 * <p>
 * With {@link org.molgenis.framework.ui.ScreenModel}s you can assemble a graphical user interface:
 * <ol>
 * <li>A user interface can be assembled as a Tree of Screen elements. Only a part of the Screens may be visible.
 * <li>Each screen in the user interface can be updated via {@link org.molgenis.framework.ui.commands.ScreenCommand}s.
 * <li>Each command is handled by a {@link org.molgenis.framework.ui.ScreenController}.
 * <li>There are several standard screens: {@link org.molgenis.framework.ui.MenuModel}, {@link org.molgenis.framework.ui.FormModel} 
 * <li>Custom screens are easily added by simply extending from {@link org.molgenis.framework.ui.HtmlPluginModel} or by implementing {@link org.molgenis.framework.ui.ScreenModel}.
 * </ol>
 * <p>
 */
package org.molgenis.framework.ui;