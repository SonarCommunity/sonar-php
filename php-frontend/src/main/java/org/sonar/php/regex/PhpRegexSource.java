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

import java.util.regex.Pattern;
import org.sonar.php.symbols.LocationInFileImpl;
import org.sonar.php.utils.LiteralUtils;
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

  /**
   * The delimiter can be any character that is not a letter, number, backslash or space.
   */
  private static final Pattern DELIMITER_PATTERN = Pattern.compile("^[^\\w\\r\\n\\t\\f\\v ]");

  public PhpRegexSource(LiteralTree stringLiteral) {
    sourceText = literalToString(stringLiteral);
    sourceLine = stringLiteral.token().line();
    sourceStartOffset = stringLiteral.token().column() + 2;
  }

  private static String literalToString(LiteralTree literal) {
    if (literal.is(Tree.Kind.REGULAR_STRING_LITERAL)) {
      return stripDelimiters(LiteralUtils.stringLiteralValue(literal.value()));
    }
    throw new IllegalArgumentException("Only string literals allowed");
  }

  private static String stripDelimiters(String pattern) {
    if (pattern.length() < 2 || !DELIMITER_PATTERN.matcher(pattern).find()) {
      throw new IllegalArgumentException("Regular expression does not contain delimiters");
    }
    return pattern.substring(1, pattern.lastIndexOf(pattern.charAt(0)));
  }

  @Override
  public CharacterParser createCharacterParser() {
    return new PhpCharacterParser(this);
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
