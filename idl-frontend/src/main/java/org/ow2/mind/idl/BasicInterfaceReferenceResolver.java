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
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.idl;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.IDLASTHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;

import com.google.inject.Inject;

public class BasicInterfaceReferenceResolver
    implements
      InterfaceReferenceResolver {

  @Inject
  protected ErrorManager       errorManagerItf;

  @Inject
  protected NodeFactory        nodeFactoryItf;

  @Inject
  protected RecursiveIDLLoader recursiveIdlLoaderItf;

  // ---------------------------------------------------------------------------
  // Overridden InterfaceReferenceResolver methods
  // ---------------------------------------------------------------------------

  public InterfaceDefinition resolve(final String itfName,
      final IDL encapsulatingIDL, final Map<Object, Object> context)
      throws ADLException {
    IDL itf;

    try {
      itf = recursiveIdlLoaderItf.load(encapsulatingIDL, itfName, context);
    } catch (final ADLException e) {
      // Log an error only if the exception is IDL_NOT_FOUND
      if (e.getError().getTemplate() == IDLErrors.IDL_NOT_FOUND) {
        errorManagerItf.logError(IDLErrors.IDL_NOT_FOUND, encapsulatingIDL,
            itfName);
      }
      return IDLASTHelper.newUnresolvedInterfaceDefinitionNode(nodeFactoryItf,
          itfName);
    }
    if (!(itf instanceof InterfaceDefinition)) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR,
          "Referenced IDL is not an interface definition");
    }
    return (InterfaceDefinition) itf;
  }
}
