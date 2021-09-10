/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2021 SonarSource SA
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
package org.sonar.php.checks.regex;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.php.regex.ast.PhpRegexBaseVisitor;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassElementTree;
import org.sonarsource.analyzer.commons.regex.ast.CharacterClassUnionTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;
import org.sonarsource.analyzer.commons.regex.helpers.SimplifiedRegexCharacterClass;

@Rule(key = "S5869")
public class DuplicatesInCharacterClassCheck extends AbstractRegexCheck {

  private static final String MESSAGE = "Remove duplicates in this character class.";

  @Override
  public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
    new DuplicateFinder().visit(regexParseResult);
  }

  private class DuplicateFinder extends PhpRegexBaseVisitor {

    @Override
    public void visitCharacterClassUnion(CharacterClassUnionTree tree) {
      Set<RegexSyntaxElement> duplicates = new LinkedHashSet<>();
      SimplifiedRegexCharacterClass characterClass = new SimplifiedRegexCharacterClass();
      for (CharacterClassElementTree element : tree.getCharacterClasses()) {
        SimplifiedRegexCharacterClass elementCharacterClass;
        try {
          elementCharacterClass = new SimplifiedRegexCharacterClass(element);
        } catch (IllegalArgumentException e) {
          // TODO: remove exception catching once the underlying problem is fixed: https://github.com/SonarSource/sonar-analyzer-commons/issues/156
          return;
        }

        List<RegexSyntaxElement> intersections = elementCharacterClass.findIntersections(characterClass);
        if (!intersections.isEmpty()) {
          // The element the current element is intersecting with should be included as well.
          duplicates.addAll(intersections);
          duplicates.add(element);
        }
        characterClass.add(element);
      }
      if (!duplicates.isEmpty()) {
        List<RegexIssueLocation> secondaries = duplicates.stream()
          .skip(1)
          .map(duplicate -> new RegexIssueLocation(duplicate, "Additional duplicate"))
          .collect(Collectors.toList());
        newIssue(duplicates.iterator().next(), MESSAGE, secondaries);
      }
      super.visitCharacterClassUnion(tree);
    }

  }
}
