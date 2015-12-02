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
package org.eclipse.che.ide.extension.maven.server.projecttype.handler;

import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.project.server.DefaultProjectManager;
import org.eclipse.che.api.project.server.Project;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.type.AbstractProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.VirtualFileSystemRegistry;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUser;
import org.eclipse.che.api.vfs.server.VirtualFileSystemUserContext;
import org.eclipse.che.api.vfs.server.impl.memory.MemoryFileSystemProvider;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * @author Roman Nikitenko
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoveMavenModuleHandlerTest {

    private static final String workspace   = "my_ws";
    private static final String apiEndpoint = "http://localhost:8080/che/api";

    private static final String POM_XML_TEMPL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                "<project>\n" +
                                                "    <modelVersion>4.0.0</modelVersion>\n" +
                                                "    <artifactId>artifact-id</artifactId>\n" +
                                                "    <groupId>group-id</groupId>\n" +
                                                "    <version>x.x.x</version>\n" +
                                                "    <modules>\n" +
                                                "        <module>firstModule</module>\n" +
                                                "        <module>secondModule</module>\n" +
                                                "    </modules>\n" +
                                                "</project>";
    private static final String FIRST_MODULE  = "firstModule";
    private static final String SECOND_MODULE = "secondModule";

    private RemoveMavenModuleHandler removeMavenModuleHandler;
    private DefaultProjectManager    projectManager;

    @Before
    public void setUp() throws Exception {
        removeMavenModuleHandler = new RemoveMavenModuleHandler();
        AbstractProjectType mavenProjectType = Mockito.mock(AbstractProjectType.class);
        Mockito.when(mavenProjectType.getId()).thenReturn(MavenAttributes.MAVEN_ID);
        Mockito.when(mavenProjectType.getDisplayName()).thenReturn(MavenAttributes.MAVEN_ID);
        Mockito.when(mavenProjectType.canBePrimary()).thenReturn(true);
        final String vfsUser = "dev";
        final Set<String> vfsUserGroups = new LinkedHashSet<>(Arrays.asList("workspace/developer"));
        final EventService eventService = new EventService();
        VirtualFileSystemRegistry vfsRegistry = new VirtualFileSystemRegistry();
        final MemoryFileSystemProvider memoryFileSystemProvider =
                new MemoryFileSystemProvider(workspace, eventService, new VirtualFileSystemUserContext() {
                    @Override
                    public VirtualFileSystemUser getVirtualFileSystemUser() {
                        return new VirtualFileSystemUser(vfsUser, vfsUserGroups);
                    }
                }, vfsRegistry);
        vfsRegistry.registerProvider(workspace, memoryFileSystemProvider);


        Set<ProjectType> projTypes = new HashSet<>();
        projTypes.add(mavenProjectType);

        ProjectTypeRegistry projectTypeRegistry = new ProjectTypeRegistry(projTypes);

        Set<ProjectHandler> handlers = new HashSet<>();
        ProjectHandlerRegistry handlerRegistry = new ProjectHandlerRegistry(handlers);

        projectManager = new DefaultProjectManager(vfsRegistry, eventService, projectTypeRegistry, handlerRegistry, apiEndpoint);

        HttpJsonHelper.HttpJsonHelperImpl httpJsonHelper = mock(HttpJsonHelper.HttpJsonHelperImpl.class);
        Field f = HttpJsonHelper.class.getDeclaredField("httpJsonHelperImpl");
        f.setAccessible(true);
        f.set(null, httpJsonHelper);
    }

    @After
    public void cleanup() throws IllegalAccessException, NoSuchFieldException {
        Field f = HttpJsonHelper.class.getDeclaredField("httpJsonHelperImpl");
        f.setAccessible(true);
        f.set(null, new HttpJsonHelper.HttpJsonHelperImpl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenPomNotFound() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        String module = NameGenerator.generate("module", 5);
        Project project =
                projectManager.createProject(workspace, parent, DtoFactory.getInstance()
                                                                          .createDto(ProjectConfigDto.class)
                                                                          .withType(MavenAttributes.MAVEN_ID), null);
        removeMavenModuleHandler
                .onRemoveModule(project.getBaseFolder(), DtoFactory.getInstance().createDto(ProjectConfigDto.class).withType("maven"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenModuleIsNotMavenModule() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        String module = NameGenerator.generate("module", 5);
        Project project =
                projectManager.createProject(workspace, parent, DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                                          .withType(MavenAttributes.MAVEN_ID), null);
        project.getBaseFolder().createFile("pom.xml", POM_XML_TEMPL.getBytes(), "text/xml");
        removeMavenModuleHandler
                .onRemoveModule(project.getBaseFolder(), DtoFactory.getInstance().createDto(ProjectConfigDto.class).withType("notmaven"));
    }

    @Test
    public void shouldRemoveModule() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        Project project =
                projectManager.createProject(workspace, parent, DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                                          .withType(MavenAttributes.MAVEN_ID), null);
        project.getBaseFolder().createFile("pom.xml", POM_XML_TEMPL.getBytes(), "text/xml");
        removeMavenModuleHandler.onRemoveModule(project.getBaseFolder(), DtoFactory.getInstance()
                                                                                   .createDto(ProjectConfigDto.class)
                                                                                   .withName(FIRST_MODULE)
                                                                                   .withType(MavenAttributes.MAVEN_ID));

        VirtualFileEntry pom = project.getBaseFolder().getChild("pom.xml");
        Assert.assertNotNull(pom);
        ContentStream content = pom.getVirtualFile().getContent();
        Assert.assertNotNull(content);
        InputStream stream = content.getStream();
        String pomContent = IoUtil.readStream(stream);
        Assert.assertNotNull(pomContent);
        Assert.assertFalse(pomContent.isEmpty());

        String firstMavenModule = String.format("<module>%s</module>", FIRST_MODULE);
        String secondMavenModule = String.format("<module>%s</module>", SECOND_MODULE);
        Assert.assertFalse(pomContent.contains(firstMavenModule));
        Assert.assertTrue(pomContent.contains(secondMavenModule));
    }

    @Test
    public void shouldNotRemoveModuleWhenPomNotContainsModule() throws Exception {
        String parent = NameGenerator.generate("parent", 5);
        String module = NameGenerator.generate("module", 5);
        Project project =
                projectManager.createProject(workspace, parent, DtoFactory.getInstance().createDto(ProjectConfigDto.class)
                                                                          .withType(MavenAttributes.MAVEN_ID), null);
        project.getBaseFolder().createFile("pom.xml", POM_XML_TEMPL.getBytes(), "text/xml");
        removeMavenModuleHandler.onRemoveModule(project.getBaseFolder(), DtoFactory.getInstance()
                                                                                   .createDto(ProjectConfigDto.class)
                                                                                   .withType(MavenAttributes.MAVEN_ID));

        VirtualFileEntry pom = project.getBaseFolder().getChild("pom.xml");
        Assert.assertNotNull(pom);
        ContentStream content = pom.getVirtualFile().getContent();
        Assert.assertNotNull(content);
        InputStream stream = content.getStream();
        String pomContent = IoUtil.readStream(stream);
        Assert.assertNotNull(pomContent);
        Assert.assertFalse(pomContent.isEmpty());

        String firstMavenModule = String.format("<module>%s</module>", FIRST_MODULE);
        String secondMavenModule = String.format("<module>%s</module>", SECOND_MODULE);
        Assert.assertTrue(pomContent.contains(firstMavenModule));
        Assert.assertTrue(pomContent.contains(secondMavenModule));
    }
}