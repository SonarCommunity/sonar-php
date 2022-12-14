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
package org.sonar.plugins.php.api.visitors;

import javax.annotation.Nullable;
import org.sonar.plugins.php.api.tree.Tree;

/**
 * @deprecated since SonarQube 9.9 and will be removed by next major version update. Use {@link PhpIssue} instead.
 */
@Deprecated(since = "9.9", forRemoval = true)
public interface Issue extends PhpIssue {

  @Override
  PHPCheck check();

  int line();

  @Override
  @Nullable
  Double cost();

  String message();

  Issue line(int line);

  Issue tree(Tree tree);

  @Override
  Issue cost(double cost);
}
