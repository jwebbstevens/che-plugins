/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.importer.page;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.util.NameUtils;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Nikitenko
 */
public class GitImporterPagePresenter extends AbstractWizardPage<ProjectConfigDto> implements GitImporterPageView.ActionDelegate {

    // An alternative scp-like syntax: [user@]host.xz:path/to/repo.git/
    private static final RegExp SCP_LIKE_SYNTAX = RegExp.compile("([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+:");
    // the transport protocol
    private static final RegExp PROTOCOL        = RegExp.compile("((http|https|git|ssh|ftp|ftps)://)");
    // the address of the remote server between // and /
    private static final RegExp HOST1           = RegExp.compile("//([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+/");
    // the address of the remote server between @ and : or /
    private static final RegExp HOST2           = RegExp.compile("@([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+[:/]");
    // the repository name
    private static final RegExp REPO_NAME       = RegExp.compile("/[A-Za-z0-9_.\\-]+$");
    // start with white space
    private static final RegExp WHITE_SPACE     = RegExp.compile("^\\s");

    private GitLocalizationConstant locale;
    private GitImporterPageView     view;

    @Inject
    public GitImporterPagePresenter(GitImporterPageView view,
                                    GitLocalizationConstant locale) {
        this.view = view;
        this.view.setDelegate(this);
        this.locale = locale;
    }

    @Override
    public boolean isCompleted() {
        return isGitUrlCorrect(dataObject.getSource().getLocation());
    }

    @Override
    public void projectNameChanged(@NotNull String name) {
        dataObject.setName(name);
        updateDelegate.updateControls();

        validateProjectName();
    }

    private void validateProjectName() {
        if (NameUtils.checkProjectName(view.getProjectName())) {
            view.hideNameError();
        } else {
            view.showNameError();
        }
    }

    @Override
    public void projectUrlChanged(@NotNull String url) {
        dataObject.getSource().setLocation(url);
        isGitUrlCorrect(url);

        String projectName = view.getProjectName();
        if (projectName.isEmpty()) {
            projectName = extractProjectNameFromUri(url);

            dataObject.setName(projectName);
            view.setProjectName(projectName);
            validateProjectName();
        }

        updateDelegate.updateControls();
    }

    @Override
    public void projectDescriptionChanged(@NotNull String projectDescription) {
        dataObject.setDescription(projectDescription);
        updateDelegate.updateControls();
    }

    /**
     * Returns project parameters map.
     *
     * @return parameters map
     */
    private Map<String, String> projectParameters() {
        Map<String, String> parameters = dataObject.getSource().getParameters();
        if (parameters == null) {
            parameters = new HashMap<>();
            dataObject.getSource().setParameters(parameters);
        }

        return parameters;
    }

    @Override
    public void keepDirectorySelected(boolean keepDirectory) {
        view.enableDirectoryNameField(keepDirectory);

        if (keepDirectory) {
            projectParameters().put("keepDirectory", view.getDirectoryName());
            dataObject.withType("blank");
            view.highlightDirectoryNameField(!NameUtils.checkProjectName(view.getDirectoryName()));
            view.focusDirectoryNameFiend();
        } else {
            projectParameters().remove("keepDirectory");
            dataObject.withType(null);
            view.highlightDirectoryNameField(false);
        }
    }

    @Override
    public void keepDirectoryNameChanged(@NotNull String directoryName) {
        if (view.keepDirectory()) {
            projectParameters().put("keepDirectory", directoryName);
            dataObject.setPath(view.getDirectoryName());
            dataObject.withType("blank");
            view.highlightDirectoryNameField(!NameUtils.checkProjectName(view.getDirectoryName()));
        } else {
            projectParameters().remove("keepDirectory");
            dataObject.setPath(null);
            dataObject.withType(null);
            view.highlightDirectoryNameField(false);
        }
    }

    @Override
    public void branchSelected(boolean branch) {
        view.enableBranchNameField(branch);

        if (view.isBranchName()) {
            projectParameters().put("branch", view.getBranchName());
        } else {
            projectParameters().remove("branch");
        }
    }

    @Override
    public void branchNameChanged(@NotNull String branch) {
        if (view.isBranchName()) {
            projectParameters().put("branch", branch);
        } else {
            projectParameters().remove("branch");
        }
    }

    @Override
    public void go(@NotNull AcceptsOneWidget container) {
        container.setWidget(view);

        view.setProjectName(dataObject.getName());
        view.setProjectDescription(dataObject.getDescription());
        view.setProjectUrl(dataObject.getSource().getLocation());

        view.setKeepDirectoryChecked(false);
        view.setBranchChecked(false);
        view.setDirectoryName("");
        view.setBranchName("");
        view.enableDirectoryNameField(false);
        view.enableBranchNameField(false);
        view.highlightDirectoryNameField(false);

        view.setInputsEnableState(true);
        view.focusInUrlInput();
    }

    /** Gets project name from uri. */
    private String extractProjectNameFromUri(@NotNull String uri) {
        int indexFinishProjectName = uri.lastIndexOf(".");
        int indexStartProjectName = uri.lastIndexOf("/") != -1 ? uri.lastIndexOf("/") + 1 : (uri.lastIndexOf(":") + 1);

        if (indexStartProjectName != 0 && indexStartProjectName < indexFinishProjectName) {
            return uri.substring(indexStartProjectName, indexFinishProjectName);
        }
        if (indexStartProjectName != 0) {
            return uri.substring(indexStartProjectName);
        }
        return "";
    }

    /**
     * Validate url
     *
     * @param url
     *         url for validate
     * @return <code>true</code> if url is correct
     */
    private boolean isGitUrlCorrect(@NotNull String url) {
        if (WHITE_SPACE.test(url)) {
            view.showUrlError(locale.importProjectMessageStartWithWhiteSpace());
            return false;
        }
        if (SCP_LIKE_SYNTAX.test(url)) {
            view.hideUrlError();
            return true;
        }
        if (!PROTOCOL.test(url)) {
            view.showUrlError(locale.importProjectMessageProtocolIncorrect());
            return false;
        }
        if (!(HOST1.test(url) || HOST2.test(url))) {
            view.showUrlError(locale.importProjectMessageHostIncorrect());
            return false;
        }
        if (!(REPO_NAME.test(url))) {
            view.showUrlError(locale.importProjectMessageNameRepoIncorrect());
            return false;
        }
        view.hideUrlError();
        return true;
    }
}
