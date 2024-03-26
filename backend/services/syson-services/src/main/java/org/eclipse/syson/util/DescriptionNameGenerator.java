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
package org.eclipse.syson.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.sysml.SysmlPackage;

/**
 * Name generator for all SysON description providers.
 *
 * @author arichard
 */
public class DescriptionNameGenerator implements IDescriptionNameGenerator {

    private static final Pattern WORD_FINDER = Pattern.compile("(([A-Z]?[a-z]+)|([A-Z]))");

    private final String diagramPrefix;

    public DescriptionNameGenerator(String diagramPrefix) {
        this.diagramPrefix = diagramPrefix;
    }

    protected String getName(String prefix, String descType, String type) {
        StringBuilder name = new StringBuilder();
        name.append(prefix)
            .append(" ")
            .append(descType)
            .append(" ")
            .append(type);
        return name.toString();
    }

    protected String getNodeName(String prefix, String type) {
        return this.getName(prefix, "Node", type);
    }

    protected String getCompartmentName(String prefix, String type) {
        return this.getName(prefix, "Compartment", type);
    }

    protected String getCompartmentItemName(String prefix, String type) {
        return this.getName(prefix, "CompartmentItem", type);
    }

    protected String getEdgeName(String prefix, String type) {
        return this.getName(prefix, "Edge", type);
    }

    /**
     * Returns the name of the creation tool of the given {@link EClassifier} with a specified prefix.
     *
     * @param prefix the string that should be prepended to the name of the given {@link EClassifier}
     * @param eClassifier the {@link EClassifier} the creation tool is in charge of.
     * @return a string starting with the given prefix and followed by the name of the given {@link EClassifier}.<br>
     * If the given classifier is a usage, the word "Usage" is removed from the name of the classifier.
     */
    @Override
    public String getCreationToolName(String prefix, EClassifier eClassifier) {
        String nameToParse = eClassifier.getName();
        if (eClassifier instanceof EClass eClass) {
            if (SysmlPackage.eINSTANCE.getUsage().isSuperTypeOf(eClass)
                    && !SysmlPackage.eINSTANCE.getConnectorAsUsage().equals(eClass)
                    && !SysmlPackage.eINSTANCE.getBindingConnectorAsUsage().equals(eClass)
                    && !SysmlPackage.eINSTANCE.getSuccessionAsUsage().equals(eClass)) {
                if (eClass.getName().endsWith("Usage")) {
                    nameToParse = eClass.getName().substring(0, eClass.getName().length() - 5);
                }
            }
        }
        return prefix + this.findWordsInMixedCase(nameToParse).stream().collect(Collectors.joining(" "));
    }

    private List<String> findWordsInMixedCase(String text) {
        Matcher matcher = WORD_FINDER.matcher(text);
        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group(0));
        }
        return words;
    }

    /**
     * Returns the name of a creation tool of the given {@link EClassifier}.
     *
     * @param eClassifier the {@link EClassifier} the creation tool is in charge of.
     * @return a string starting with the word {@code NEW} and followed by the name of the given {@link EClassifier}.
     */
    @Override
    public String getCreationToolName(EClassifier eClassifier) {
        return this.getCreationToolName("New ", eClassifier);
    }

    /**
     * Returns the name of a {@link NodeDescription} starting with the diagram prefix and followed by the given string.
     * @param type a string to form the name of the node description.
     * @return a string starting with the diagram prefix and followed by the given string.
     */
    @Override
    public String getNodeName(String type) {
        return this.getNodeName(this.diagramPrefix, type);
    }

    /**
     * Returns the name of a {@link NodeDescription} starting with the diagram prefix and followed by the name of the given {@link EClass}.
     *
     * @param eClass the {@link EClass} used to compute the name of the {@link NodeDescription}.
     * @return a string starting with the diagram prefix and followed by the name of the given {@link EClass}
     */
    @Override
    public String getNodeName(EClass eClass) {
        return this.getNodeName(this.diagramPrefix, eClass.getName());
    }

    /**
     * Returns the name of a compartment {@link NodeDescription} starting with the diagram prefix, followed by the name of the given {@link EClass} and the name of the given {@link EReference}.
     * @param eClass the {@link EClass} used to compute the name of the {@link NodeDescription}.
     * @param eReference the {@link EReference} that the compartment is containing.
     * @return a string starting with the diagram prefix, followed by the name of the given {@link EClass} and the name of the given {@link EReference}
     */
    @Override
    public String getCompartmentName(EClass eClass, EReference eReference) {
        return this.getCompartmentName(this.diagramPrefix, eClass.getName() + " " + eReference.getName());
    }

    /**
     * Returns the name of a compartment items {@link NodeDescription} starting with the diagram prefix, followed by the name of the given {@link EClass} and the name of the given {@link EReference}.
     * @param eClass the {@link EClass} used to compute the name of the {@link NodeDescription}.
     * @param eReference the {@link EReference} that the compartment is containing.
     * @return a string starting with the diagram prefix, followed by the name of the given {@link EClass} and the name of the given {@link EReference}
     */
    @Override
    public String getCompartmentItemName(EClass eClass, EReference eReference) {
        return this.getCompartmentItemName(this.diagramPrefix, eClass.getName() + " " + eReference.getName());
    }

    /**
     * Returns the name of a {@link EdgeDescription} starting with the diagram prefix and followed by the given edge type.
     * @param type a string representing the name of the edge.
     * @return a string starting with the diagram prefix and followed by the given edge type.
     */
    @Override
    public String getEdgeName(String type) {
        return this.getEdgeName(this.diagramPrefix, type);
    }
}