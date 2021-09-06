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
package org.sonar.php.regex;

import java.util.HashMap;
import java.util.Map;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.visitors.LocationInFile;
import org.sonarsource.analyzer.commons.regex.CharacterParser;
import org.sonarsource.analyzer.commons.regex.RegexDialect;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.ast.IndexRange;

public class PhpRegexSource implements RegexSource {

  private final String sourceText;
  private final int sourceLine;
  private final int sourceStartOffset;
  private final char quote;

  public PhpRegexSource(LiteralTree stringLiteral) {
    quote = stringLiteral.value().charAt(0);
    sourceText = literalToString(stringLiteral);
    sourceLine = stringLiteral.token().line();
    sourceStartOffset = stringLiteral.token().column() + 2;
  }

  private static String literalToString(LiteralTree literal) {
    if (literal.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      String literalValue = literal.value();
      String valueWithoutQuotes = literalValue.substring(1, literalValue.length() - 1);
      return stripDelimiters(valueWithoutQuotes);
    }
    throw new IllegalArgumentException("Only string literals allowed");
  }

  private static String stripDelimiters(String pattern) {
    if (pattern.length() >= 2) {
      Character endDelimiter = PhpRegexUtils.getEndDelimiter(pattern);
      return pattern.substring(1, pattern.lastIndexOf(endDelimiter));
    }
    throw new IllegalArgumentException("Regular expression does not contain delimiters");
  }

  @Override
  public CharacterParser createCharacterParser() {
    if (quote == '\'') {
      return PhpStringCharacterParser.forSingleQuotedString(this);
    }
    return PhpStringCharacterParser.forDoubleQuotedString(this);
  }

  @Override
  public String getSourceText() {
    return sourceText;
  }

  @Override
  public RegexDialect dialect() {
    return RegexDialect.PHP;
  }

  public LocationInFile locationInFileFor(IndexRange range) {
    return new LocationInFileImpl(null, sourceLine, sourceStartOffset + range.getBeginningOffset(), sourceLine,sourceStartOffset + range.getEndingOffset());
  }
}
