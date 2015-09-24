/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.gwt.client.command;

import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectNameProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.DevMachineHostNameProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

/** @author Artem Zatsarynnyy */
@RunWith(MockitoJUnitRunner.class)
public class GwtCommandTypeTest {

    @Mock
    private GwtResources               gwtResources;
    @Mock
    private GwtCommandPagePresenter    gwtCommandPagePresenter;
    @Mock
    private CurrentProjectNameProvider currentProjectNameProvider;
    @Mock
    private DevMachineHostNameProvider devMachineHostNameProvider;

    @InjectMocks
    private GwtCommandType gwtCommandType;

    @Test
    public void shouldReturnIcon() throws Exception {
        gwtCommandType.getIcon();

        verify(gwtResources).gwtCommandType();
    }

    @Test
    public void shouldReturnPages() throws Exception {
        final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages = gwtCommandType.getConfigurationPages();

        assertTrue(pages.contains(gwtCommandPagePresenter));
    }

    @Test
    public void shouldReturnCommandTemplate() throws Exception {
        gwtCommandType.getCommandTemplate();

        verify(currentProjectNameProvider).getKey();
        verify(devMachineHostNameProvider).getKey();
    }
}