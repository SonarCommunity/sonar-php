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

import java.util.List;
import org.junit.Test;
import org.sonar.plugins.php.api.visitors.LocationInFile;
import org.sonarsource.analyzer.commons.regex.RegexDialect;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;
import org.sonarsource.analyzer.commons.regex.RegexParser;
import org.sonarsource.analyzer.commons.regex.RegexSource;
import org.sonarsource.analyzer.commons.regex.SyntaxError;
import org.sonarsource.analyzer.commons.regex.ast.CharacterTree;
import org.sonarsource.analyzer.commons.regex.ast.FlagSet;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.sonar.php.regex.RegexParserTestUtils.assertKind;
import static org.sonar.php.regex.RegexParserTestUtils.assertSuccessfulParse;
import static org.sonar.php.regex.RegexParserTestUtils.makeSource;

public class PhpRegexSourceTest {

  @Test
  // TODO: Extend test with exact syntax error location check
  public void invalid_regex() {
    RegexSource source = makeSource("'/+/'");
    RegexParseResult result = new RegexParser(source, new FlagSet()).parse();

    assertThat(result.getSyntaxErrors()).isNotEmpty();
  }

  @Test
  public void test_to_few_delimiters() {
    assertThatThrownBy(() -> makeSource("'/'"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Regular expression does not contain delimiters");
  }

  @Test
  public void test_non_string_literal() {
    assertThatThrownBy(() -> makeSource("1"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Only string literals allowed");
  }

  @Test
  public void test_string_literal() {
    RegexTree regex = assertSuccessfulParse("'/a\nb/'"); // <?php foo('/a\nb/');
    assertKind(RegexTree.Kind.SEQUENCE, regex);
    List<RegexTree> items = ((SequenceTree) regex).getItems();
    assertThat(items).hasSize(3);

    assertCharacter('a', items.get(0));
    assertCharacter('\n', items.get(1));
    assertCharacter('b', items.get(2));

    assertLocation(3, 2, 3, items.get(0));
    assertLocation(3, 3, 4, items.get(1));
    assertLocation(3, 4, 5, items.get(2));
  }

  @Test
  public void single_quote_vs_double_quote() {
    RegexParseResult singleQuoted = new RegexParser(makeSource("'/\\u{0041}/'"), new FlagSet()).parse();
    assertThat(singleQuoted.getSyntaxErrors()).extracting(SyntaxError::getMessage).containsExactly("Expected hexadecimal digit, but found '{'");

    RegexParseResult doubleQuoted = new RegexParser(makeSource("\"/\\u{0041}/\""), new FlagSet()).parse();
    assertThat(doubleQuoted.getSyntaxErrors()).isEmpty();
    assertThat(doubleQuoted.getResult().kind()).isEqualTo(RegexTree.Kind.CHARACTER);
    assertThat(((CharacterTree) doubleQuoted.getResult()).characterAsString()).isEqualTo("A");
  }

  @Test
  public void test_string_literal_with_bracket_delimiters() {
    RegexTree regex = assertSuccessfulParse("'[a]'");
    assertKind(RegexTree.Kind.CHARACTER, regex);
    assertCharacter('a', regex);
    assertLocation(3, 2, 3, regex);
  }

  @Test
  public void test_dialect() {
    RegexSource source = makeSource("'/a/'");
    assertEquals(RegexDialect.PHP, source.dialect());
  }

  @Test
  public void php_literal_escape_sequence() {
    RegexTree regex = assertSuccessfulParse("'/a\\\\\\\\b/'");
    assertKind(RegexTree.Kind.SEQUENCE, regex);
    List<RegexTree> items = ((SequenceTree) regex).getItems();
    assertThat(items).allMatch(t -> t.is(RegexTree.Kind.CHARACTER))
      .extracting(t -> ((CharacterTree) t).characterAsString())
      .containsExactly("a", "\\", "b");
    assertLocation(3, 3, 7, items.get(1));
  }

  private static void assertCharacter(char expected, RegexTree tree) {
    assertKind(RegexTree.Kind.CHARACTER, tree);
    assertEquals(expected, ((CharacterTree) tree).codePointOrUnit());
  }

  private static void assertLocation(int line, int startLineOffset, int endLineOffset, RegexTree tree) {
    LocationInFile location = ((PhpRegexSource) tree.getSource()).locationInFileFor(tree.getRange());
    assertLocation(line, startLineOffset, endLineOffset, location);
  }

  private static void assertLocation(int line, int startLineOffset, int endLineOffset, LocationInFile location) {
    assertEquals(String.format("Expected line to be '%d' but got '%d'", line, location.startLine()), line, location.startLine());
    assertEquals(String.format("Expected line to be '%d' but got '%d'", line, location.endLine()), line, location.endLine());
    assertEquals(String.format("Expected start character to be '%d' but got '%d'", startLineOffset, location.startLineOffset()), startLineOffset, location.startLineOffset());
    assertEquals(String.format("Expected end character to be '%d' but got '%d'", endLineOffset, location.endLineOffset()), endLineOffset, location.endLineOffset());
  }

}
