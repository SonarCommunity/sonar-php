/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.php.api.tree.declaration;

import org.sonar.plugins.php.api.tree.SeparatedList;

/**
 * <a href="https://wiki.php.net/rfc/dnf_types">DNF Types</a>
 *
 * @since 3.39
 */
public interface DnfTypeTree extends DeclaredTypeTree {

  /**
   * The list of elements and separators, e.g., <code>int</code>, <code>|</code> and <code>(A&amp;B)</code> in <code>int|(A&amp;B)</code>.
   * @return the list of elements and separators
   */
  SeparatedList<DeclaredTypeTree> types();
}
