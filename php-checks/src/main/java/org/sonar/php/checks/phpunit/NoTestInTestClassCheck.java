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
package org.sonar.php.checks.phpunit;

import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.php.checks.utils.PhpUnitCheck;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.ClassMemberTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;

@Rule(key = "S2187")
public class NoTestInTestClassCheck extends PhpUnitCheck {

  private static final String MESSAGE = "Add some tests to this class.";

  @Override
  protected void visitPhpUnitTestCase(ClassDeclarationTree tree) {
    if (!CheckUtils.isAbstract(tree)) {
      boolean hasTestMethod = false;
      for (ClassMemberTree member : tree.members()) {
        if (member.is(Tree.Kind.METHOD_DECLARATION) && isTestCaseMethod((MethodDeclarationTree) member)) {
          hasTestMethod = true;
          break;
        }
      }

      if (!hasTestMethod) {
        newIssue(tree.name(), MESSAGE);
      }
    }

    super.visitPhpUnitTestCase(tree);
  }
}
