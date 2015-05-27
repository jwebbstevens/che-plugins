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
package org.eclipse.che.ide.extension.machine.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineDescriptor;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.extension.machine.client.actions.EditCommandsAction;
import org.eclipse.che.ide.extension.machine.client.actions.ExecuteArbitraryCommandAction;
import org.eclipse.che.ide.extension.machine.client.actions.ExecuteSelectedCommandAction;
import org.eclipse.che.ide.extension.machine.client.actions.SelectCommandAction;
import org.eclipse.che.ide.extension.machine.client.actions.TerminateMachineAction;
import org.eclipse.che.ide.extension.machine.client.machine.MachineManager;
import org.eclipse.che.ide.extension.machine.client.machine.console.ClearConsoleAction;
import org.eclipse.che.ide.extension.machine.client.machine.console.MachineConsolePresenter;
import org.eclipse.che.ide.extension.machine.client.machine.console.MachineConsoleToolbar;
import org.eclipse.che.ide.extension.machine.client.outputspanel.OutputsContainerPresenter;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;

import java.util.List;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CODE;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RIGHT_TOOLBAR;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;

/**
 * Machine extension entry point.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
@Extension(title = "Machine", version = "1.0.0")
public class MachineExtension {

    public static final String GROUP_MACHINE_CONSOLE_TOOLBAR = "MachineConsoleToolbar";
    public static final String GROUP_MACHINE_TOOLBAR         = "MachineGroupToolbar";
    public static final String GROUP_COMMANDS_LIST           = "CommandsListGroup";

    @Inject
    public MachineExtension(final MachineResources machineResources,
                            final EventBus eventBus,
                            final MachineServiceClient machineServiceClient,
                            final MachineManager machineManager,
                            final MachineConsolePresenter machineConsolePresenter) {
        machineResources.getCss().ensureInjected();

        eventBus.addHandler(ProjectActionEvent.TYPE, new ProjectActionHandler() {
            @Override
            public void onProjectOpened(ProjectActionEvent event) {
                // start machine and bind project
                final String projectPath = event.getProject().getPath();
                Promise<List<MachineDescriptor>> machinesPromise = machineServiceClient.getMachines(projectPath);
                machinesPromise.then(new Operation<List<MachineDescriptor>>() {
                    @Override
                    public void apply(List<MachineDescriptor> arg) throws OperationException {
                        if (arg.isEmpty()) {
                            machineManager.startMachineAndBindProject(projectPath);
                        } else {
                            machineManager.setCurrentMachineId(arg.get(0).getId());
                        }
                    }
                });
            }

            @Override
            public void onProjectClosing(ProjectActionEvent event) {
            }

            @Override
            public void onProjectClosed(ProjectActionEvent event) {
                machineManager.setCurrentMachineId(null);
                machineConsolePresenter.clear();
            }
        });
    }

    @Inject
    private void prepareActions(MachineLocalizationConstant localizationConstant,
                                ActionManager actionManager,
                                ExecuteArbitraryCommandAction executeArbitraryCommandAction,
                                ExecuteSelectedCommandAction executeSelectedCommandAction,
                                SelectCommandAction selectCommandAction,
                                EditCommandsAction editCommandsAction,
                                TerminateMachineAction terminateMachineAction) {
        final DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);
        final DefaultActionGroup runMenu = new DefaultActionGroup(localizationConstant.mainMenuRunName(), true, actionManager);

        // register actions
        actionManager.registerAction("run", runMenu);
        actionManager.registerAction("executeArbitraryCommand", executeArbitraryCommandAction);
        actionManager.registerAction("editCommands", editCommandsAction);
        actionManager.registerAction("terminateMachine", terminateMachineAction);
        actionManager.registerAction("selectCommandAction", selectCommandAction);
        actionManager.registerAction("executeSelectedCommand", executeSelectedCommandAction);

        // add actions in main menu
        mainMenu.add(runMenu, new Constraints(AFTER, GROUP_CODE));
        runMenu.add(executeArbitraryCommandAction);
        runMenu.add(editCommandsAction);
        runMenu.addSeparator();
        runMenu.add(terminateMachineAction);

        // add actions on right toolbar
        final DefaultActionGroup rightToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RIGHT_TOOLBAR);
        final DefaultActionGroup machineToolbarGroup = new DefaultActionGroup(GROUP_MACHINE_TOOLBAR, false, actionManager);
        actionManager.registerAction(GROUP_MACHINE_TOOLBAR, machineToolbarGroup);
        rightToolbarGroup.add(machineToolbarGroup);
        machineToolbarGroup.add(selectCommandAction);
        machineToolbarGroup.add(executeSelectedCommandAction);

        // add group for command list
        final DefaultActionGroup commandList = new DefaultActionGroup(GROUP_COMMANDS_LIST, false, actionManager);
        actionManager.registerAction(GROUP_COMMANDS_LIST, commandList);
        commandList.add(editCommandsAction, FIRST);
        commandList.addSeparator();
    }

    @Inject
    private void setUpMachineConsole(ActionManager actionManager,
                                     ClearConsoleAction clearConsoleAction,
                                     WorkspaceAgent workspaceAgent,
                                     MachineConsolePresenter machineConsolePresenter,
                                     @MachineConsoleToolbar ToolbarPresenter machineConsoleToolbar) {
        workspaceAgent.openPart(machineConsolePresenter, PartStackType.INFORMATION);

        // add toolbar to Machine console
        final DefaultActionGroup consoleToolbarActionGroup = new DefaultActionGroup(GROUP_MACHINE_CONSOLE_TOOLBAR, false, actionManager);
        consoleToolbarActionGroup.add(clearConsoleAction);
        consoleToolbarActionGroup.addSeparator();
        machineConsoleToolbar.bindMainGroup(consoleToolbarActionGroup);
    }

    @Inject
    private void setUpOutputsConsole(WorkspaceAgent workspaceAgent,
                                     OutputsContainerPresenter outputsContainerPresenter) {
        workspaceAgent.openPart(outputsContainerPresenter, PartStackType.INFORMATION);
    }
}
