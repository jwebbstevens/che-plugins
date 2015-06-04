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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.terminal;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class TerminalViewImplTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private Machine machine;

    @InjectMocks
    private TerminalViewImpl view;

    @Test
    public void terminalShouldBeUpdated() {
        when(machine.getTerminalUrl()).thenReturn(SOME_TEXT);

        view.updateTerminal(machine);

        verify(machine).getTerminalUrl();
        verify(view.terminal).setUrl(SOME_TEXT);
    }
}