/*******************************************************************************
 * Copyright (c) 2024 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.syson.application.configuration;

import static org.eclipse.syson.application.configuration.SysMLStandardLibrariesConfiguration.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreAdapterFactory;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Tests about loading of SysML Standard libraries.
 * 
 * @author arichard
 */
public class SysMLEditingContextProcessorTest {

    
    private static ResourceSet resourceSet;

    @BeforeAll
    static void loadLibraries() {
        ComposedAdapterFactory composedAdapterFactory = new ComposedAdapterFactory();
        composedAdapterFactory.addAdapterFactory(new EcoreAdapterFactory());
        composedAdapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

        EPackage.Registry ePackageRegistry = new EPackageRegistryImpl();
        ePackageRegistry.put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);

        AdapterFactoryEditingDomain editingDomain = new AdapterFactoryEditingDomain(composedAdapterFactory, new BasicCommandStack());
        resourceSet = editingDomain.getResourceSet();
        resourceSet.setPackageRegistry(ePackageRegistry);
        resourceSet.eAdapters().add(new ECrossReferenceAdapter());
        EditingContext editingContext = new EditingContext(UUID.randomUUID().toString(), editingDomain, Map.of(), List.of());
        SysMLEditingContextProcessor editingContextProcessor = new SysMLEditingContextProcessor(new SysMLStandardLibrariesConfiguration());
        editingContextProcessor.preProcess(editingContext);
        assertNotNull(resourceSet);
    }

    @Test
    void loadKerMLLibraries() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + KERML_LIBRARY_PATH + "*." + JsonResourceFactoryImpl.EXTENSION);
        for (org.springframework.core.io.Resource resource : resources) {
            String libraryFilePath = resource.getFilename();
            ClassPathResource classPathResource = new ClassPathResource(KERML_LIBRARY_PATH + libraryFilePath);
            String path = classPathResource.getPath();
            URI uri = URI.createURI(KERML_LIBRARY_SCHEME + ":///" + UUID.nameUUIDFromBytes(path.getBytes()));
            Resource emfResource = resourceSet.getResource(uri, false);
            assertNotNull(emfResource, "Unable to load " + libraryFilePath);
        }
    }

    @Test
    void loadSysMLLibraries() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + SYSML_LIBRARY_PATH + "*." + JsonResourceFactoryImpl.EXTENSION);
        for (org.springframework.core.io.Resource resource : resources) {
            String libraryFilePath = resource.getFilename();
            ClassPathResource classPathResource = new ClassPathResource(SYSML_LIBRARY_PATH + libraryFilePath);
            String path = classPathResource.getPath();
            URI uri = URI.createURI(SYSML_LIBRARY_SCHEME + ":///" + UUID.nameUUIDFromBytes(path.getBytes()));
            Resource emfResource = resourceSet.getResource(uri, false);
            assertNotNull(emfResource, "Unable to load " + libraryFilePath);
        }
    }

    @Test
    void testSysMLAliasIntegration() throws IOException {
        String libPath = "ISQSpaceTime.json";
        URI uri = getSysMLResourceURI(libPath);
        Resource emfResource = resourceSet.getResource(uri, false);
        assertNotNull(emfResource, "Unable to load " + libPath);
    }


    protected URI getSysMLResourceURI(String filePath) {
        return getResourceURI(SYSML_LIBRARY_SCHEME, SYSML_LIBRARY_PATH, filePath);
    }

    private URI getResourceURI(String libraryScheme, String libraryPath,  String filePath) {
        ClassPathResource classPathResource = new ClassPathResource(libraryPath + filePath);
        return URI.createURI(libraryScheme + ":///" + UUID.nameUUIDFromBytes(classPathResource.getPath().getBytes()));
    }
}
