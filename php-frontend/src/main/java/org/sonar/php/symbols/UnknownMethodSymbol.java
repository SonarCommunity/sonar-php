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

import org.sonar.plugins.php.api.symbols.QualifiedName;

public class UnknownMethodSymbol extends UnknownFunctionSymbol implements MethodSymbol {
  private final String name;

  public UnknownMethodSymbol(String name) {
    super(QualifiedName.qualifiedName(name));
    this.name = name;
  }

  @Override
  public Visibility visibility() {
    return Visibility.PUBLIC;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public Trilean isOverriding() {
    return Trilean.UNKNOWN;
  }

  @Override
  public Trilean isAbstract() {
    return Trilean.UNKNOWN;
  }

  @Override
  public ClassSymbol owner() {
    return new UnknownClassSymbol(QualifiedName.qualifiedName("unknown"));
  }
}
