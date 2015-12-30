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
package org.eclipse.che.ide.extension.machine.client.processes;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.ui.tree.SelectionModel;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.input.SignalEvent;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ConsolesPanelView}.
 *
 * @author Anna Shumilova
 * @author Roman Nikitenko
 */
@Singleton
public class ConsolesPanelViewImpl extends BaseView<ConsolesPanelView.ActionDelegate> implements ConsolesPanelView, RequiresResize {

    @UiField(provided = true)
    MachineResources      machineResources;

    @UiField(provided = true)
    Tree<ProcessTreeNode> processTree;

    @UiField(provided = true)
    SplitLayoutPanel      splitLayoutPanel;

    @UiField
    DeckLayoutPanel       outputPanel;

    @UiField
    FlowPanel             navigationPanel;

    private Map<String, IsWidget> processWidgets;
    private List<ProcessTreeNode> processNodes;

    @Inject
    public ConsolesPanelViewImpl(org.eclipse.che.ide.Resources resources,
                                 MachineResources machineResources,
                                 PartStackUIResources partStackUIResources,
                                 ProcessesViewImplUiBinder uiBinder,
                                 ProcessTreeRenderer renderer,
                                 ProcessDataAdapter adapter) {
        super(partStackUIResources);

        this.machineResources = machineResources;
        this.processWidgets = new HashMap<>();
        this.processNodes = new ArrayList<>();

        splitLayoutPanel = new SplitLayoutPanel(3);

        renderer.setAddTerminalClickHandler(new AddTerminalClickHandler() {
            @Override
            public void onAddTerminalClick(@NotNull String machineId) {
                delegate.onAddTerminal(machineId);
            }
        });

        renderer.setStopProcessHandler(new StopProcessHandler() {
            @Override
            public void onStopProcessClick(@NotNull ProcessTreeNode node) {
                delegate.onStopCommandProcess(node);
            }

            @Override
            public void onCloseProcessOutputClick(@NotNull ProcessTreeNode node) {
                ProcessTreeNode.ProcessNodeType type = node.getType();
                switch (type) {
                    case COMMAND_NODE:
                        delegate.onCloseCommandOutputClick(node);
                        break;
                    case TERMINAL_NODE:
                        delegate.onCloseTerminal(node);
                        break;
                }
            }
        });

        processTree = Tree.create(resources, adapter, renderer);
        processTree.asWidget().addStyleName(this.machineResources.getCss().processTree());

        processTree.setTreeEventHandler(new Tree.Listener<ProcessTreeNode>() {
            @Override
            public void onNodeAction(TreeNodeElement<ProcessTreeNode> node) {

            }

            @Override
            public void onNodeClosed(TreeNodeElement<ProcessTreeNode> node) {

            }

            @Override
            public void onNodeContextMenu(int mouseX, int mouseY, TreeNodeElement<ProcessTreeNode> node) {

            }

            @Override
            public void onNodeDragStart(TreeNodeElement<ProcessTreeNode> node, MouseEvent event) {

            }

            @Override
            public void onNodeDragDrop(TreeNodeElement<ProcessTreeNode> node, MouseEvent event) {

            }

            @Override
            public void onNodeExpanded(TreeNodeElement<ProcessTreeNode> node) {

            }

            @Override
            public void onNodeSelected(TreeNodeElement<ProcessTreeNode> node, SignalEvent event) {
                ProcessTreeNode.ProcessNodeType type = node.getData().getType();
                switch (type) {
                    case COMMAND_NODE:
                        delegate.onCommandSelected(node.getData().getId());
                        break;
                    case TERMINAL_NODE:
                        delegate.onTerminalSelected(node.getData().getId());
                        break;
                }
            }

            @Override
            public void onRootContextMenu(int mouseX, int mouseY) {

            }

            @Override
            public void onRootDragDrop(MouseEvent event) {

            }

            @Override
            public void onKeyboard(KeyboardEvent event) {

            }
        });

        setContentWidget(uiBinder.createAndBindUi(this));
        navigationPanel.getElement().setTabIndex(0);
    }

    @Override
    protected void focusView() {
        getElement().focus();
    }

    @Override
    public void addProcessWidget(String processId, IsWidget widget) {
        processWidgets.put(processId, widget);
        outputPanel.add(widget);
        showProcessOutput(processId);
    }

    @Override
    public void addProcessNode(@NotNull ProcessTreeNode node) {
        processNodes.add(node);
    }

    @Override
    public void removeProcessNode(@NotNull ProcessTreeNode node) {
        processNodes.remove(node);
    }

    @Override
    public void setProcessesData(@NotNull ProcessTreeNode root) {
        processTree.asWidget().setVisible(true);
        processTree.getModel().setRoot(root);
        processTree.renderTree(-1);
    }

    @Override
    public void selectNode(@NotNull ProcessTreeNode node) {
        SelectionModel<ProcessTreeNode> selectionModel = processTree.getSelectionModel();

        selectionModel.setTreeActive(true);
        selectionModel.clearSelections();
        selectionModel.selectSingleNode(node);

        node.getTreeNodeElement().scrollIntoView();
    }

    @Override
    public void showProcessOutput(String processId) {
        if (processWidgets.containsKey(processId)) {
            onResize();
            outputPanel.showWidget(processWidgets.get(processId).asWidget());
        }
    }

    @Override
    public void hideProcessOutput(String processId) {
        IsWidget widget = processWidgets.get(processId);
        outputPanel.remove(widget);
        processWidgets.remove(processId);
    }

    @Override
    public int getNodeIndex(String processId) {
        for (ProcessTreeNode node : processNodes) {
            if (processId.equals(node.getId())) {
                return processNodes.indexOf(node);
            }
        }
        return -1;
    }

    @Override
    @Nullable
    public ProcessTreeNode getNodeByIndex(@NotNull int index) {
        if (index < 0 || index >= processNodes.size()) {
            return null;
        }
        return processNodes.get(index);
    }

    @Override
    @Nullable
    public ProcessTreeNode getNodeById(@NotNull String nodeId) {
        for (ProcessTreeNode node : processNodes) {
            if (nodeId.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void refreshStopProcessButtonState(String nodeId) {
        for (ProcessTreeNode node : processNodes) {
            if (nodeId.equals(node.getId())) {
                node.getTreeNodeElement().getClassList().add(machineResources.getCss().selectedProcess());
            } else {
                node.getTreeNodeElement().getClassList().remove(machineResources.getCss().selectedProcess());
            }
        }
    }

    @Override
    public void onResize() {
        for (int i = 0; i < outputPanel.getWidgetCount(); i++) {
            Widget widget = outputPanel.getWidget(i);
            if (widget instanceof RequiresResize) {
                ((RequiresResize)widget).onResize();
            }
        }
    }

    interface ProcessesViewImplUiBinder extends UiBinder<Widget, ConsolesPanelViewImpl> {
    }

}