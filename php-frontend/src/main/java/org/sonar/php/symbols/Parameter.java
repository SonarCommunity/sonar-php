/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.php.symbols;

import org.sonar.plugins.php.api.tree.declaration.ParameterTree;

import javax.annotation.Nullable;

public class Parameter {
  private final String name;
  private final String type;
  private final boolean hasDefault;
  private final boolean hasEllipsisOperator;

  public Parameter(String name, @Nullable String type, boolean hasDefault, boolean hasEllipsisOperator) {
    this.type = type;
    this.name = name;
    this.hasDefault = hasDefault;
    this.hasEllipsisOperator = hasEllipsisOperator;
  }

  public static Parameter fromTree(ParameterTree parameter) {
    String parameterName = parameter.variableIdentifier().text();
    String parameterType = parameter.type() != null ? parameter.type().toString() : null;

    return new Parameter(
      parameterName,
      parameterType,
      parameter.initValue() != null,
      parameter.ellipsisToken() != null
    );
  }

  public String name() {
    return name;
  }

  public String type() {
    return type;
  }

  public boolean hasDefault() {
    return hasDefault;
  }

  public boolean hasEllipsisOperator() {
    return hasEllipsisOperator;
  }
}
