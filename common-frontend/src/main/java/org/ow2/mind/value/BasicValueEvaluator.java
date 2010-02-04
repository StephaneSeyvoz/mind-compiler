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

package org.ow2.mind.value;

import static java.lang.reflect.Array.newInstance;
import static java.lang.reflect.Array.set;
import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;

import java.util.Map;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.value.ast.Array;
import org.ow2.mind.value.ast.BooleanLiteral;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;
import org.ow2.mind.value.ast.ValueHelper;

public class BasicValueEvaluator implements ValueEvaluator, BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  public static final String RECURSIVE_VALUE_EVALUATOR_ITF_NAME = "recursive-evaluator";
  public ValueEvaluator      recursiveEvaluatorItf;

  // ---------------------------------------------------------------------------
  // Implementation of the ValueEvaluator interface
  // ---------------------------------------------------------------------------

  public <T> T evaluate(final Value value, final Class<T> expectedType,
      final Map<Object, Object> context) throws ValueEvaluationException {
    if (value instanceof NumberLiteral) {
      if (expectedType.isPrimitive()) {
        return evaluatePrimitiveType((NumberLiteral) value, expectedType);
      }

      Number n;
      final String v = ((NumberLiteral) value).getValue();
      try {
        if (expectedType == Byte.class) {
          n = Byte.valueOf(v);
        } else if (expectedType == Short.class) {
          n = Short.valueOf(v);
        } else if (expectedType == Long.class) {
          n = Long.valueOf(v);
        } else if (expectedType == Float.class) {
          n = Float.valueOf(v);
        } else if (expectedType == Double.class) {
          n = Double.valueOf(v);
        } else {
          // default case
          n = Integer.valueOf(v);
        }
      } catch (final NumberFormatException e) {
        throw new ValueEvaluationException(
            "Value of NumberLiteral node is not a number", value, e);
      }

      try {
        return expectedType.cast(n);
      } catch (final ClassCastException e) {
        throw new ValueEvaluationException(
            "Incompatible value type, found number where "
                + expectedType.getName() + " was expected", value, e);
      }
    } else if (value instanceof StringLiteral) {
      String s = ((StringLiteral) value).getValue();
      s = s.replaceAll("\\\\\"", "\"");
      try {
        return expectedType.cast(s);
      } catch (final ClassCastException e) {
        throw new ValueEvaluationException(
            "Incompatible value type, found string where "
                + expectedType.getName() + " was expected", value, e);
      }

    } else if (value instanceof BooleanLiteral) {
      if (expectedType.isPrimitive()) {
        return evaluatePrimitiveType((BooleanLiteral) value, expectedType);
      }
      try {
        return expectedType.cast(Boolean.valueOf(ValueHelper
            .getValue((BooleanLiteral) value)));
      } catch (final ClassCastException e) {
        throw new ValueEvaluationException(
            "Incompatible value type, found boolean where "
                + expectedType.getName() + " was expected", value, e);
      }
    } else if (value instanceof Array) {
      if (!expectedType.isArray()) {
        throw new ValueEvaluationException(
            "Incompatible value type, found array where "
                + expectedType.getName() + " was expected", value);
      }
      final Class<?> arrayComponentType = expectedType.getComponentType();
      final Value[] arrayValues = ((Array) value).getValues();
      final Object result = newInstance(arrayComponentType, arrayValues.length);
      for (int i = 0; i < arrayValues.length; i++) {
        set(result, i, recursiveEvaluatorItf.evaluate(arrayValues[i],
            arrayComponentType, context));
      }
      return expectedType.cast(result);
    } else {
      throw new ValueEvaluationException("Unknow value type", value);
    }

  }

  @SuppressWarnings("unchecked")
  protected <T> T evaluatePrimitiveType(final NumberLiteral value,
      final Class<T> expectedType) throws ValueEvaluationException {
    final String v = value.getValue();
    try {
      if (expectedType == Byte.TYPE) {
        return (T) Byte.valueOf(v);
      } else if (expectedType == Short.TYPE) {
        return (T) Short.valueOf(v);
      } else if (expectedType == Integer.TYPE) {
        return (T) Integer.valueOf(v);
      } else if (expectedType == Long.TYPE) {
        return (T) Long.valueOf(v);
      } else if (expectedType == Float.TYPE) {
        return (T) Float.valueOf(v);
      } else if (expectedType == Double.TYPE) {
        return (T) Double.valueOf(v);
      } else {
        throw new ValueEvaluationException(
            "Incompatible value type, found number where "
                + expectedType.getName() + " was expected", value);
      }
    } catch (final NumberFormatException e) {
      throw new ValueEvaluationException(
          "Value of NumberLiteral node is not a number", value, e);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> T evaluatePrimitiveType(final BooleanLiteral value,
      final Class<T> expectedType) throws ValueEvaluationException {
    final String v = value.getValue();
    try {
      if (expectedType == Boolean.TYPE) {
        return (T) Boolean.valueOf(v);
      } else {
        throw new ValueEvaluationException(
            "Incompatible value type, found boolean where "
                + expectedType.getName() + " was expected", value);
      }
    } catch (final NumberFormatException e) {
      throw new ValueEvaluationException(
          "Value of NumberLiteral node is not a number", value, e);
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public String[] listFc() {
    return listFcHelper(RECURSIVE_VALUE_EVALUATOR_ITF_NAME);
  }

  public Object lookupFc(final String s) throws NoSuchInterfaceException {
    checkItfName(s);

    if (RECURSIVE_VALUE_EVALUATOR_ITF_NAME.equals(s)) {
      return recursiveEvaluatorItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

  public void bindFc(final String s, final Object o)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(s);

    if (RECURSIVE_VALUE_EVALUATOR_ITF_NAME.equals(s)) {
      recursiveEvaluatorItf = (ValueEvaluator) o;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "' for binding the interface");
    }
  }

  public void unbindFc(final String s) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(s);

    if (RECURSIVE_VALUE_EVALUATOR_ITF_NAME.equals(s)) {
      recursiveEvaluatorItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '" + s
          + "'");
    }
  }

}
