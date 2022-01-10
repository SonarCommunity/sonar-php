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
package org.sonar.php.checks.regex;

import com.sonar.sslr.api.typed.ActionParser;
import java.util.Collections;
import org.junit.Test;
import org.sonar.php.parser.PHPLexicalGrammar;
import org.sonar.php.parser.PHPParserBuilder;
import org.sonar.php.tree.visitors.PHPCheckContext;
import org.sonar.plugins.php.CheckVerifier;
import org.sonar.plugins.php.api.tree.CompilationUnitTree;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.expression.ExpressionTree;
import org.sonar.plugins.php.api.tree.expression.FunctionCallTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.statement.ExpressionStatementTree;
import org.sonar.plugins.php.api.visitors.PHPCheck;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;
import org.sonar.plugins.php.api.visitors.PhpFile;
import org.sonarsource.analyzer.commons.regex.RegexParseResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class AbstractRegexCheckTest {

  private final ActionParser<Tree> parser = PHPParserBuilder.createParser(PHPLexicalGrammar.TOP_STATEMENT);

  @Test
  public void test_getLiteral() {
    assertThat(AbstractRegexCheck.getLiteral(expr("'//'"))).isPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("'/a/'"))).isPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("' /a/'"))).isPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("'aFooa'"))).isNotPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("''"))).isNotPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("'[FOO]'"))).isPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("'[FOO'"))).isNotPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("$unknownPattern"))).isNotPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("FOO"))).isNotPresent();
    assertThat(AbstractRegexCheck.getLiteral(expr("'[FOO['"))).isNotPresent();
  }

  @Test
  public void test_getFlagSet() {
    assertThat(AbstractRegexCheck.getFlagSet((LiteralTree) expr("\"/a/\"")).isEmpty()).isTrue();
    assertThat(AbstractRegexCheck.getFlagSet((LiteralTree) expr("\"/a/i\""))).satisfies(f -> {
      assertThat(f.isEmpty()).isFalse();
      assertThat(f.contains(AbstractRegexCheck.PCRE_CASELESS)).isTrue();
    });

    assertThat(AbstractRegexCheck.getFlagSet((LiteralTree) expr("\"/a/ixmsu\""))).satisfies(f -> {
      assertThat(f.contains(AbstractRegexCheck.PCRE_CASELESS)).isTrue();
      assertThat(f.contains(AbstractRegexCheck.PCRE_MULTILINE)).isTrue();
      assertThat(f.contains(AbstractRegexCheck.PCRE_DOTALL)).isTrue();
      assertThat(f.contains(AbstractRegexCheck.PCRE_UTF8)).isTrue();
      assertThat(f.contains(AbstractRegexCheck.PCRE_EXTENDED)).isTrue();
    });

    assertThat(AbstractRegexCheck.getFlagSet((LiteralTree) expr("\"/a/U\"")).isEmpty()).isTrue();
    assertThat(AbstractRegexCheck.getFlagSet((LiteralTree) expr("\"  /a/i\"")).getMask()).isEqualTo(AbstractRegexCheck.PCRE_CASELESS);
  }

  @Test
  public void test_newIssue_on_regexFunctionCall_with_cost() {
    AbstractRegexCheck check = new AbstractRegexCheck() {
      @Override
      public void checkRegex(RegexParseResult regexParseResult, FunctionCallTree regexFunctionCall) {
        newIssue(regexFunctionCall, "TestMessage", 1, Collections.emptyList());
      }
    };
    CheckVerifier.verify(check, "regex/AbstractRegexCheck.php");
  }

  private ExpressionTree expr(String pattern) {
    return ((ExpressionStatementTree) parse(String.format("%s;", pattern))).expression();
  }

  private Tree parse(String toParse) {
    return parser.parse(toParse);
  }
}
