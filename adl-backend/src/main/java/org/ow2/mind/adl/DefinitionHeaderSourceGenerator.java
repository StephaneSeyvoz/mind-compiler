/**
 * Copyright (C) 2009 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Ali Erdem Ozcan, Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.adl;

import static org.ow2.mind.PathHelper.fullyQualifiedNameToPath;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.ow2.mind.SourceFileWriter;
import org.ow2.mind.io.IOErrors;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * {@link DefinitionSourceGenerator} component that generated {@value #FILE_EXT}
 * files using the {@value #DEFAULT_TEMPLATE} template.
 */
public class DefinitionHeaderSourceGenerator extends AbstractSourceGenerator
    implements
      DefinitionSourceGenerator {

  /** The name to be used to inject the templateGroupName used by this class. */
  public static final String    TEMPLATE_NAME    = "definitions.header";

  /** The default templateGroupName used by this class. */
  public static final String    DEFAULT_TEMPLATE = "st.definitions.header.Component";

  protected final static String FILE_EXT         = ".adl.h";

  @Inject
  protected DefinitionHeaderSourceGenerator(
      @Named(TEMPLATE_NAME) final String templateGroupName) {
    super(templateGroupName);
  }

  /**
   * A static method that returns the name of the file that is generated by this
   * component for the given {@link Definition};
   * 
   * @param definition a {@link Definition} node.
   * @return the name of the file that is generated by this component for the
   *         given {@link Definition};
   */
  public static String getHeaderFileName(final Definition definition) {
    return fullyQualifiedNameToPath(definition.getName(), FILE_EXT);
  }

  // ---------------------------------------------------------------------------
  // Implementation of the DefinitionSourceGenerator interface
  // ---------------------------------------------------------------------------

  public void visit(final Definition definition,
      final Map<Object, Object> context) throws ADLException {
    final File outputFile = outputFileLocatorItf.getCSourceOutputFile(
        getHeaderFileName(definition), context);

    if (regenerate(outputFile, definition, context)) {

      final StringTemplate st = getInstanceOf("ComponentDefinitionHeader");
      st.setAttribute("definition", definition);

      try {
        SourceFileWriter.writeToFile(outputFile, st.toString());
      } catch (final IOException e) {
        throw new CompilerError(IOErrors.WRITE_ERROR, e,
            outputFile.getAbsolutePath());
      }
    }
  }
}
