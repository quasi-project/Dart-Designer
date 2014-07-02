/*******************************************************************************
 * Copyright (c) 2014 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.obeonetwork.dsl.dart.design.internal.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.viewpoint.DSemanticDecorator;
import org.obeonetwork.dsl.dart.Application;
import org.obeonetwork.dsl.dart.Class;
import org.obeonetwork.dsl.dart.Container;
import org.obeonetwork.dsl.dart.DartResource;
import org.obeonetwork.dsl.dart.Export;
import org.obeonetwork.dsl.dart.Field;
import org.obeonetwork.dsl.dart.Folder;
import org.obeonetwork.dsl.dart.Import;
import org.obeonetwork.dsl.dart.Library;
import org.obeonetwork.dsl.dart.Metadata;
import org.obeonetwork.dsl.dart.Method;
import org.obeonetwork.dsl.dart.Package;
import org.obeonetwork.dsl.dart.Parameter;
import org.obeonetwork.dsl.dart.Type;
import org.obeonetwork.dsl.dart.design.internal.utils.I18n;
import org.obeonetwork.dsl.dart.design.internal.utils.I18nKeys;

/**
 * Utility services for the class diagram.
 *
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 */
public class DartClassDiagramServices {
	/**
	 * The type of an element with a null type.
	 */
	private static final String VOID = "void"; //$NON-NLS-1$

	/**
	 * Returns the name of the newly created class diagram for the given container.
	 *
	 * @param container
	 *            The container
	 * @return The name of the class diagram
	 */
	public String newClassDiagramName(Container container) {
		String containerName = ""; //$NON-NLS-1$
		if (container instanceof Folder) {
			containerName = ((Folder)container).getName();
		} else if (container instanceof Package) {
			containerName = ((Package)container).getName();
		}
		return I18n.getString(I18nKeys.CLASS_DIAGRAM_NEW_NAME, containerName).trim();
	}

	/**
	 * Returns the list of all the elements that can be added to the class diagram.
	 *
	 * @param eObject
	 *            The currently selected eObject (the project in the background most of the time)
	 * @param containerView
	 *            The container view
	 * @return The list of Dart elements that can be added to the class diagram
	 */
	public List<EObject> getAddableClassDiagramElements(EObject eObject, DSemanticDecorator containerView) {
		List<EObject> result = new ArrayList<EObject>();

		Session session = SessionManager.INSTANCE.getSession(eObject);
		if (session != null) {
			Collection<Resource> semanticResources = session.getSemanticResources();
			for (Resource resource : semanticResources) {
				if (resource.getURI().isPlatformPlugin() || resource.getURI().isPlatformResource()) {
					TreeIterator<EObject> allContents = resource.getAllContents();
					while (allContents.hasNext()) {
						EObject containedEObject = allContents.next();
						if (containedEObject instanceof Class) {
							result.add(containedEObject);
						} else if (containedEObject instanceof Metadata) {
							result.add(containedEObject);
						} else if (containedEObject instanceof Library) {
							result.add(containedEObject);
						} else if (containedEObject instanceof Application) {
							result.add(containedEObject);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns the label of the field.
	 *
	 * @param eObject
	 *            The field
	 * @return The label of the field
	 */
	public String getFieldLabel(EObject eObject) {
		if (eObject instanceof Field) {
			Field field = (Field)eObject;

			StringBuilder builder = new StringBuilder();
			if (field.getName() != null) {
				builder.append(field.getName());
			}
			builder.append(':');
			if (field.getType() == null) {
				builder.append(VOID);
			} else {
				Type type = field.getType();
				if (type instanceof Class) {
					Class aClass = (Class)type;
					builder.append(aClass.getName());
				}
			}
			return builder.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the label of the method.
	 *
	 * @param eObject
	 *            The method
	 * @return The label of the method
	 */
	public String getMethodLabel(EObject eObject) {
		if (eObject instanceof Method) {
			Method method = (Method)eObject;

			StringBuilder builder = new StringBuilder();
			if (method.getName() != null) {
				builder.append(method.getName());
			}

			builder.append('(');
			int count = 0;
			final int size = method.getParameters().size();
			for (Parameter parameter : method.getParameters()) {
				builder.append(parameter.getName());
				builder.append(':');

				if (parameter.getType() == null) {
					builder.append(VOID);
				} else {
					Type type = parameter.getType();
					if (type instanceof Class) {
						Class aClass = (Class)type;
						builder.append(aClass.getName());
					}
				}

				if (count + 1 < size) {
					builder.append(", "); //$NON-NLS-1$
				}
				count++;
			}
			builder.append(')');

			builder.append(':');
			if (method.getType() == null) {
				builder.append(VOID);
			} else {
				Type type = method.getType();
				if (type instanceof Class) {
					Class aClass = (Class)type;
					builder.append(aClass.getName());
				}
			}
			return builder.toString();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the types of all the fields.
	 *
	 * @param eObject
	 *            The class
	 * @return The types of all the fields
	 */
	public List<EObject> getFieldTypes(EObject eObject) {
		List<EObject> types = new ArrayList<EObject>();
		if (eObject instanceof Class) {
			Class aClass = (Class)eObject;
			List<Field> fields = aClass.getFields();
			for (Field field : fields) {
				Type type = field.getType();
				if (type != null) {
					types.add(type);
				}
			}
		}
		return types;
	}

	/**
	 * Returns the list of Dart resources imported by another Dart resource.
	 *
	 * @param eObject
	 *            The Dart resource
	 * @return The list of Dart resources imported by another Dart resource
	 */
	public List<DartResource> getImportedResources(EObject eObject) {
		List<DartResource> importedResources = new ArrayList<DartResource>();
		if (eObject instanceof DartResource) {
			DartResource dartResource = (DartResource)eObject;
			List<Import> imports = dartResource.getImports();
			for (Import anImport : imports) {
				DartResource importedDartResource = anImport.getDartResource();
				if (importedDartResource != null && !anImport.getHide().contains(importedDartResource)
						&& (anImport.getShow().size() == 0 || anImport.getShow().contains(anImport))) {
					importedResources.add(importedDartResource);
				}
			}

		}
		return importedResources;
	}

	/**
	 * Returns the list of Dart resources exported by another Dart resource.
	 *
	 * @param eObject
	 *            The Dart resource
	 * @return The list of Dart resources exported by another Dart resource
	 */
	public List<DartResource> getExportedResources(EObject eObject) {
		List<DartResource> exportedResources = new ArrayList<DartResource>();
		if (eObject instanceof DartResource) {
			DartResource dartResource = (DartResource)eObject;
			List<Export> exports = dartResource.getExports();
			for (Export anExport : exports) {
				DartResource exportedDartResource = anExport.getDartResource();
				if (exportedDartResource != null && !anExport.getHide().contains(exportedDartResource)
						&& (anExport.getShow().size() == 0 || anExport.getShow().contains(anExport))) {
					exportedResources.add(exportedDartResource);
				}
			}

		}
		return exportedResources;
	}
}
