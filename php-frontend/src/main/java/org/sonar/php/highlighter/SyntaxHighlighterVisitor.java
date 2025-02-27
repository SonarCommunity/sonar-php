/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2025 SonarSource SA
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
package org.sonar.php.highlighter;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.php.api.PHPKeyword;
import org.sonar.php.utils.collections.ListUtils;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringCharactersTree;
import org.sonar.plugins.php.api.tree.expression.ExpandableStringLiteralTree;
import org.sonar.plugins.php.api.tree.expression.LiteralTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

public class SyntaxHighlighterVisitor extends PHPVisitorCheck {

  private static final List<String> PHP_RESERVED_VARIABLES = Arrays.asList(
    "__FUNCTION__",
    "__CLASS__",
    "__METHOD__",
    "__NAMESPACE__",
    "__DIR__",
    "__FILE__",
    "__LINE__",
    "$this");

  private static final List<String> WORDS_TO_HIGHLIGHT = ListUtils.concat(Arrays.asList(PHPKeyword.getKeywordValues()), PHP_RESERVED_VARIABLES);

  private NewHighlighting highlighting;

  private SyntaxHighlighterVisitor(NewHighlighting highlighting) {
    this.highlighting = highlighting;
  }

  public static void highlight(Tree tree, NewHighlighting highlighting) {
    SyntaxHighlighterVisitor visitor = new SyntaxHighlighterVisitor(highlighting);
    visitor.scan(tree);
  }

  @Override
  public void visitToken(SyntaxToken token) {
    if (token.is(Kind.TOKEN) && WORDS_TO_HIGHLIGHT.contains(token.text())) {
      highlight(token, TypeOfText.KEYWORD);
    }

    super.visitToken(token);
  }

  @Override
  public void visitTrivia(SyntaxTrivia trivia) {
    if (trivia.text().startsWith("/**")) {
      highlight(trivia, TypeOfText.STRUCTURED_COMMENT);
    } else {
      highlight(trivia, TypeOfText.COMMENT);
    }
  }

  @Override
  public void visitExpandableStringLiteral(ExpandableStringLiteralTree tree) {
    highlight(tree.openDoubleQuoteToken(), TypeOfText.STRING);
    highlight(tree.closeDoubleQuoteToken(), TypeOfText.STRING);

    super.visitExpandableStringLiteral(tree);
  }

  @Override
  public void visitLiteral(LiteralTree tree) {
    if (tree.is(Kind.REGULAR_STRING_LITERAL)) {
      highlight(tree.token(), TypeOfText.STRING);

    } else if (tree.is(Kind.NUMERIC_LITERAL)) {
      highlight(tree.token(), TypeOfText.CONSTANT);
    }

    super.visitLiteral(tree);
  }

  @Override
  public void visitExpandableStringCharacters(ExpandableStringCharactersTree tree) {
    highlight(tree.token(), TypeOfText.STRING);
    super.visitExpandableStringCharacters(tree);
  }

  private void highlight(SyntaxToken token, TypeOfText typeOfText) {
    highlighting.highlight(token.line(), token.column(), token.endLine(), token.endColumn(), typeOfText);
  }

}
