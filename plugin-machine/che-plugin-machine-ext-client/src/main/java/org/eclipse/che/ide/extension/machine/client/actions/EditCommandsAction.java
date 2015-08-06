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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsPresenter;

import javax.annotation.Nonnull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action for editing commands.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class EditCommandsAction extends AbstractPerspectiveAction {
    private final AppContext            appContext;
    private final EditCommandsPresenter presenter;
    private final AnalyticsEventLogger  eventLogger;

    @Inject
    public EditCommandsAction(EditCommandsPresenter presenter,
                              MachineLocalizationConstant localizationConstant,
                              MachineResources resources,
                              AppContext appContext,
                              AnalyticsEventLogger eventLogger) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstant.editCommandsControlTitle(),
              localizationConstant.editCommandsControlDescription(),
              null,
              resources.recipe());

        this.presenter = presenter;
        this.appContext = appContext;
        this.eventLogger = eventLogger;
    }

    @Override
    public void updateInPerspective(@Nonnull ActionEvent e) {
        e.getPresentation().setVisible(appContext.getCurrentProject() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
        presenter.show();
    }
}