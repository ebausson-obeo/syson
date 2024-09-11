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
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.*;

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
import org.eclipse.syson.sysml.*;
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
            Resource emfResource = resourceSet.getResource(getKerMLResourceURI(libraryFilePath), false);
            assertNotNull(emfResource, "Unable to load " + libraryFilePath);
        }
    }

    @Test
    void loadSysMLLibraries() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        org.springframework.core.io.Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + SYSML_LIBRARY_PATH + "*." + JsonResourceFactoryImpl.EXTENSION);
        for (org.springframework.core.io.Resource resource : resources) {
            String libraryFilePath = resource.getFilename();
            Resource emfResource = resourceSet.getResource(getSysMLResourceURI(libraryFilePath), false);
            assertNotNull(emfResource, "Unable to load " + libraryFilePath);
        }
    }

    @Test
    void testSysMLIntegration() throws IOException {
        Resource emfResource = resourceSet.getResource(getSysMLResourceURI("ISQSpaceTime.json"), false);
        HashMap<String, Element> namedContent = parseNamedContent(emfResource);

//       Membership
        // testing Alias
        Element breadthElement = namedContent.get("breadth");
        assertNotNull(breadthElement, "Unable to find Element 'breadth' in library ISQSpaceTime.");
        assertInstanceOf(Membership.class, breadthElement, "Element 'breadth' in library ISQSpaceTime is not a Membership.");
        Membership membership = (Membership) breadthElement;
        assertEquals("width", membership.getMemberElement().getDeclaredName());

//      FeatureTyping
        Element frequencyValueElement = namedContent.get("FrequencyValue");
        assertNotNull(frequencyValueElement, "Unable to find Element 'FrequencyValue' in library ISQSpaceTime.");
        assertInstanceOf(FeatureTyping.class, frequencyValueElement, "Element 'FrequencyValue' in library ISQSpaceTime is not a FeatureTyping.");
        FeatureTyping featureTyping = (FeatureTyping) frequencyValueElement;
        assertInstanceOf(AttributeDefinition.class, featureTyping.getType());

//      MembershipImport
        Element scalarValuesRealElement = namedContent.get("ScalarValues::Real");
        assertNotNull(scalarValuesRealElement, "Unable to find Element 'ScalarValues::Real' in library ISQSpaceTime.");
        assertInstanceOf(MembershipImport.class, scalarValuesRealElement);
        MembershipImport scalarValuesReal = (MembershipImport) scalarValuesRealElement;
        assertEquals(VisibilityKind.PRIVATE, scalarValuesReal.getVisibility());
        assertEquals("ScalarValues::Real", scalarValuesReal.getDeclaredName());

//      AttributeUsage
        Element azimuthElement = namedContent.get("azimuth");
        assertNotNull(azimuthElement, "Unable to find Element 'azimuth' in library ISQSpaceTime.");
        assertInstanceOf(AttributeUsage.class, azimuthElement);
        AttributeUsage azimuth = (AttributeUsage) azimuthElement;
        assertEquals("azimuth", azimuth.getDeclaredName());
        assertEquals("φ", azimuth.getDeclaredShortName());
        assertEquals(2, azimuth.getOwnedRelationship().size());

//      Subclassification
        Element position3dvectorElement = namedContent.get("Position3dVector");
        assertNotNull(position3dvectorElement, "Unable to find Element 'Position3dVector' in library ISQSpaceTime.");
        assertInstanceOf(Subclassification.class, position3dvectorElement);
        Subclassification position3dvector = (Subclassification) position3dvectorElement;
        assertEquals("Position3dVector", position3dvector.getDeclaredName());
        assertEquals(0, position3dvector.getOwnedRelationship().size());

//      Subsetting
        Element vectorQuantitiesElement = namedContent.get("vectorQuantities");
        assertNotNull(vectorQuantitiesElement, "Unable to find Element 'vectorQuantities' in library ISQSpaceTime.");
        assertInstanceOf(Subsetting.class, vectorQuantitiesElement);
        Subsetting vectorQuantities = (Subsetting) vectorQuantitiesElement;
        assertEquals("vectorQuantities", vectorQuantities.getDeclaredName());
        assertEquals(0, vectorQuantities.getOwnedRelationship().size());

//      NamespaceImport
        Element measurementReferencesElement = namedContent.get("MeasurementReferences");
        assertNotNull(measurementReferencesElement, "Unable to find Element 'MeasurementReferences' in library ISQSpaceTime.");
        assertInstanceOf(NamespaceImport.class, measurementReferencesElement);
        NamespaceImport measurementReferences = (NamespaceImport) measurementReferencesElement;
        assertEquals(VisibilityKind.PRIVATE, scalarValuesReal.getVisibility());
        assertEquals("MeasurementReferences", measurementReferences.getDeclaredName());
        assertEquals(0, measurementReferences.getOwnedRelationship().size());

//      AttributeDefinition
        Element attributeDefinitionElement = namedContent.get("PhaseVelocityUnit");
        assertNotNull(attributeDefinitionElement, "Unable to find Element 'PhaseVelocityUnit' in library ISQSpaceTime.");
        assertInstanceOf(AttributeDefinition.class, attributeDefinitionElement);
        AttributeDefinition attributeDefinition = (AttributeDefinition) attributeDefinitionElement;
        assertEquals(VisibilityKind.PRIVATE, scalarValuesReal.getVisibility());
        assertEquals("PhaseVelocityUnit", attributeDefinition.getDeclaredName());
        assertEquals(4, attributeDefinition.getOwnedRelationship().size());

//      Redefinition
        Element redefinitionElement = namedContent.get("exponent");
        assertNotNull(redefinitionElement, "Unable to find Element 'exponent' in library ISQSpaceTime.");
        assertInstanceOf(Redefinition.class, redefinitionElement);
        Redefinition redefinition = (Redefinition) redefinitionElement;
        assertEquals("exponent", redefinition.getDeclaredName());
        assertEquals(0, redefinition.getOwnedRelationship().size());

//      LibraryPackage
        Element libraryPackageElement = namedContent.get("ISQSpaceTime");
        assertNotNull(libraryPackageElement, "Unable to find LibraryPackage Element 'ISQSpaceTime'.");
        assertInstanceOf(LibraryPackage.class, libraryPackageElement);
        LibraryPackage libraryPackage = (LibraryPackage) libraryPackageElement;
        assertEquals(VisibilityKind.PRIVATE, scalarValuesReal.getVisibility());
        assertEquals("ISQSpaceTime", libraryPackage.getDeclaredName());
        assertEquals(186, libraryPackage.getOwnedRelationship().size());
    }


    protected HashMap<String, Element> parseNamedContent(Resource resource) {
        resource.getContents();
        HashMap<String, Element> namedContent = new HashMap<>();
        resource.getAllContents().forEachRemaining(eObject -> {
            if (eObject instanceof Element element && element.getDeclaredName() != null) {
                namedContent.put(element.getDeclaredName(), element);
            }
        });
        return namedContent;
    }

    protected URI getKerMLResourceURI(String filePath) {
        return getResourceURI(KERML_LIBRARY_SCHEME, KERML_LIBRARY_PATH, filePath);
    }

    protected URI getSysMLResourceURI(String filePath) {
        return getResourceURI(SYSML_LIBRARY_SCHEME, SYSML_LIBRARY_PATH, filePath);
    }

    private URI getResourceURI(String libraryScheme, String libraryPath,  String filePath) {
        ClassPathResource classPathResource = new ClassPathResource(libraryPath + filePath);
        return URI.createURI(libraryScheme + ":///" + UUID.nameUUIDFromBytes(classPathResource.getPath().getBytes()));
    }
}
