package net.sourceforge.squirrel_sql.client.mainframe.action;

/*
 * Copyright (C) 2007 Rob Manning
 * manningr@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import net.sourceforge.squirrel_sql.client.IApplication;
import net.sourceforge.squirrel_sql.client.action.SquirrelAction;
import net.sourceforge.squirrel_sql.fw.gui.DialogUtils;

import java.awt.event.ActionEvent;

/**
 * This <CODE>Action</CODE> allows the user to save the application state in 
 * terms of preferences and properties.
 */
public class SavePreferencesAction extends SquirrelAction
{

    /**
     * Ctor specifying the list of aliases.
     * 
     * @param app
     *            Application API.           
     */
    public SavePreferencesAction(IApplication app) {
        super(app);
    }

    /**
     * Perform this action. Use the <TT>DeleteAliasCommand</TT>.
     * 
     * @param evt
     *            The current event.
     */
    public void actionPerformed(ActionEvent evt) {
        IApplication app = getApplication();
        SavePreferencesCommand command = 
            new SavePreferencesCommand(app, getParentFrame(evt));
        command.setDialogUtils(new DialogUtils());
        command.execute();
    }
}
