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
package org.sonar.php.regex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.sonarsource.analyzer.commons.regex.RegexIssueLocation;
import org.sonarsource.analyzer.commons.regex.ast.CharacterTree;
import org.sonarsource.analyzer.commons.regex.ast.RegexSyntaxElement;
import org.sonarsource.analyzer.commons.regex.ast.RegexTree;
import org.sonarsource.analyzer.commons.regex.ast.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.sonar.php.regex.RegexParserTestUtils.assertKind;
import static org.sonar.php.regex.RegexParserTestUtils.assertSuccessfulParse;

public class PhpRegexCheckTest {

  @Test
  public void regexLocationsToIssueLocations() {
    // force a separation
    RegexTree regex = assertSuccessfulParse("'/AB/'");
    assertKind(RegexTree.Kind.SEQUENCE, regex);

    List<RegexTree> items = ((SequenceTree) regex).getItems();
    assertThat(items)
      .hasSize(2)
      .allMatch(tree -> tree.is(RegexTree.Kind.CHARACTER));

    assertRange(2, 4, correspondingTextSpans(regex));

    CharacterTree char1 = (CharacterTree) items.get(0);

    // empty filtered out
    assertRange(2, 3, correspondingTextSpans(char1));

    CharacterTree char2 = (CharacterTree) items.get(1);
    assertRange(3, 4, correspondingTextSpans(char2));

  }

  @Test
  public void locationOfMultipleRegexSyntaxElement() {
    // force a separation
    RegexTree regex = assertSuccessfulParse("'/ABC/'");
    assertKind(RegexTree.Kind.SEQUENCE, regex);

    List<RegexTree> items = ((SequenceTree) regex).getItems();
    assertThat(items).hasSize(3);

    RegexTree A = items.get(0);
    RegexTree B = items.get(1);
    RegexTree C = items.get(2);

    assertRange(2, 5, correspondingTextSpans(Arrays.asList(A, B, C)));
    assertRange(2, 4, correspondingTextSpans(Arrays.asList(A, B)));
    assertRange(2, 3, correspondingTextSpans(Arrays.asList(A, C)));
  }

  @Test
  public void emptyRegex() {
    RegexTree regex = assertSuccessfulParse("'//'");
    assertKind(RegexTree.Kind.SEQUENCE, regex);
    assertThat(((SequenceTree) regex).getItems()).isEmpty();

    assertRange(2, 2, correspondingTextSpans(regex));
  }

  private void assertRange(int startLineOffset, int endLineOffset, PhpRegexCheck.PhpRegexIssueLocation location) {
    assertEquals(String.format("Expected start character to be '%d' but got '%d'", startLineOffset, location.startLineOffset()), startLineOffset, location.startLineOffset());
    assertEquals(String.format("Expected end character to be '%d' but got '%d'", endLineOffset, location.endLineOffset()), endLineOffset, location.endLineOffset());
  }

  private static PhpRegexCheck.PhpRegexIssueLocation correspondingTextSpans(RegexTree tree) {
    return correspondingTextSpans(Collections.singletonList(tree));
  }

  private static PhpRegexCheck.PhpRegexIssueLocation correspondingTextSpans(List<RegexSyntaxElement> trees) {
    return new PhpRegexCheck.PhpRegexIssueLocation(new RegexIssueLocation(trees, "message"));
  }

}
