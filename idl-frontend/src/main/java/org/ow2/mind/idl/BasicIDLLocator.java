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

import static org.objectweb.fractal.adl.util.ClassLoaderHelper.getClassLoader;
import static org.ow2.mind.PathHelper.fullyQualifiedNameToPath;
import static org.ow2.mind.PathHelper.isRelative;
import static org.ow2.mind.PathHelper.isValid;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.ow2.mind.InputResource;
import org.ow2.mind.NameHelper;

/**
 * Basic implementation of the {@link IDLLocator} interface for MIND IDL.
 */
public class BasicIDLLocator implements IDLLocator {

  protected static final String SOURCE_ITF_EXTENSION = ".itf";
  protected static final String BINARY_ITF_EXTENSION = ".idef";

  // ---------------------------------------------------------------------------
  // Implementation of the IDLLocator interface
  // ---------------------------------------------------------------------------

  public URL[] getInputResourcesRoot(final Map<Object, Object> context) {
    final ClassLoader cl = ClassLoaderHelper.getClassLoader(this, context);
    if (cl instanceof URLClassLoader) {
      return ((URLClassLoader) cl).getURLs();
    }
    return null;
  }

  public URL findSourceItf(final String name, final Map<Object, Object> context) {
    if (!NameHelper.isValid(name))
      throw new IllegalArgumentException("\"" + name + "\" is not a valid name");

    return getClassLoader(this, context).getResource(
        fullyQualifiedNameToPath(name, SOURCE_ITF_EXTENSION).substring(1));
  }

  public URL findBinaryItf(final String name, final Map<Object, Object> context) {
    if (!NameHelper.isValid(name))
      throw new IllegalArgumentException("\"" + name + "\" is not a valid name");

    return getClassLoader(this, context).getResource(
        fullyQualifiedNameToPath(name, SOURCE_ITF_EXTENSION).substring(1));
  }

  public URL findHeader(final String path, final Map<Object, Object> context) {
    if (!isValid(path))
      throw new IllegalArgumentException("\"" + path + "\" is not a valid path");
    if (isRelative(path))
      throw new IllegalArgumentException("\"" + path
          + "\" is not an absolute path");

    return getClassLoader(this, context).getResource(path.substring(1));
  }

  public URL findResource(final String name, final Map<Object, Object> context) {
    if (name.startsWith("/"))
      return findHeader(name, context);
    else
      return findSourceItf(name, context);
  }

  public InputResource toInterfaceInputResource(final String name) {
    return new InputResource(IDLLocator.ITF_RESOURCE_KIND, name);
  }

  public InputResource toSharedTypeInputResource(final String name) {
    return new InputResource(IDLLocator.IDT_RESOURCE_KIND, name);
  }
}
