/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Lists every concrete class of the module being compiled under
 * {@code META-INF/services/<type>} for each of its supertypes that carries
 * {@code org.diylc.core.annotations.Indexed}. The application reads these files at startup instead
 * of scanning the classpath.
 *
 * @author Branislav Stojkovic
 */
@SupportedAnnotationTypes("*")
public class ServiceIndexProcessor extends AbstractProcessor {

  private static final String INDEXED_ANNOTATION = "org.diylc.core.annotations.Indexed";
  private static final String SERVICES_DIR = "META-INF/services/";

  private final Map<String, Set<String>> index = new TreeMap<String, Set<String>>();

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      writeIndex();
    } else {
      for (Element element : roundEnv.getRootElements()) {
        collect(element);
      }
    }
    return false;
  }

  private void collect(Element element) {
    if (!(element instanceof TypeElement)) {
      return;
    }
    TypeElement type = (TypeElement) element;
    if (isInstantiable(type)) {
      String className = processingEnv.getElementUtils().getBinaryName(type).toString();
      for (String service : findIndexedSupertypes(type)) {
        index.computeIfAbsent(service, k -> new TreeSet<String>()).add(className);
      }
    }
    for (Element enclosed : type.getEnclosedElements()) {
      if (enclosed.getModifiers().contains(Modifier.STATIC)) {
        collect(enclosed);
      }
    }
  }

  private boolean isInstantiable(TypeElement type) {
    return type.getKind() == ElementKind.CLASS && !type.getModifiers().contains(Modifier.ABSTRACT);
  }

  private Set<String> findIndexedSupertypes(TypeElement type) {
    Set<String> result = new HashSet<String>();
    Set<String> visited = new HashSet<String>();
    Deque<TypeMirror> pending = new ArrayDeque<TypeMirror>();
    pending.add(type.asType());
    while (!pending.isEmpty()) {
      for (TypeMirror supertype : processingEnv.getTypeUtils().directSupertypes(pending.poll())) {
        if (supertype.getKind() != TypeKind.DECLARED) {
          continue;
        }
        TypeElement supertypeElement = (TypeElement) ((DeclaredType) supertype).asElement();
        String name = processingEnv.getElementUtils().getBinaryName(supertypeElement).toString();
        if (!visited.add(name)) {
          continue;
        }
        if (isIndexed(supertypeElement)) {
          result.add(name);
        }
        pending.add(supertype);
      }
    }
    return result;
  }

  private boolean isIndexed(TypeElement type) {
    for (AnnotationMirror annotation : type.getAnnotationMirrors()) {
      TypeElement annotationType = (TypeElement) annotation.getAnnotationType().asElement();
      if (annotationType.getQualifiedName().contentEquals(INDEXED_ANNOTATION)) {
        return true;
      }
    }
    return false;
  }

  private void writeIndex() {
    for (Map.Entry<String, Set<String>> entry : index.entrySet()) {
      String path = SERVICES_DIR + entry.getKey();
      Set<String> classNames = new TreeSet<String>(entry.getValue());
      // an incremental build (e.g. in the IDE) compiles only the changed classes, so carry over
      // the entries from the previous build that still qualify
      classNames.addAll(readPreviousEntries(path, entry.getKey()));
      try {
        FileObject file =
            processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path);
        try (Writer writer =
            new OutputStreamWriter(file.openOutputStream(), StandardCharsets.UTF_8)) {
          for (String className : classNames) {
            writer.write(className);
            writer.write('\n');
          }
        }
      } catch (IOException e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
            "Could not write " + path + ": " + e.getMessage());
      }
    }
  }

  private Set<String> readPreviousEntries(String path, String service) {
    Set<String> result = new TreeSet<String>();
    try {
      FileObject file =
          processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path);
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(file.openInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (!line.isEmpty() && stillQualifies(line, service)) {
            result.add(line);
          }
        }
      }
    } catch (IOException e) {
      // no previous build output
    }
    return result;
  }

  private boolean stillQualifies(String className, String service) {
    TypeElement type = processingEnv.getElementUtils().getTypeElement(className.replace('$', '.'));
    return type != null && isInstantiable(type) && findIndexedSupertypes(type).contains(service);
  }
}
