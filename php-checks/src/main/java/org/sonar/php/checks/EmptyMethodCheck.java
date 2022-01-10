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
package org.sonar.php.checks;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.php.tree.TreeUtils;
import org.sonar.php.tree.impl.PHPTree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.tree.declaration.ClassDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionDeclarationTree;
import org.sonar.plugins.php.api.tree.declaration.FunctionTree;
import org.sonar.plugins.php.api.tree.declaration.MethodDeclarationTree;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.php.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.php.api.tree.statement.BlockTree;
import org.sonar.plugins.php.api.visitors.PHPVisitorCheck;

@Rule(key = "S1186")
public class EmptyMethodCheck extends PHPVisitorCheck {

  private static final String MESSAGE = "Add a nested comment explaining why this %s is empty, throw an Exception or complete the implementation.";

  private static final int MIN_WORD_CHARS = 3;
  private static final Pattern VALUABLE_COMMENT_PATTERN = Pattern.compile("\\w{"+ MIN_WORD_CHARS +"}");


  @Override
  public void visitMethodDeclaration(MethodDeclarationTree tree) {
    if (tree.body().is(Kind.BLOCK) && !(hasValuableBody((BlockTree) tree.body()) || isClassAbstract(tree)
      || hasCommentAbove(((PHPTree) tree).getFirstToken()) || isConstructorPropertyPromotion(tree))) {
      commitIssue(tree, "method");
    }

    super.visitMethodDeclaration(tree);
  }

  @Override
  public void visitFunctionDeclaration(FunctionDeclarationTree tree) {
    if (!(hasValuableBody(tree.body()) || hasCommentAbove(((PHPTree) tree).getFirstToken()))) {
      commitIssue(tree, "function");
    }

    super.visitFunctionDeclaration(tree);
  }

  private static boolean hasCommentAbove(SyntaxToken token) {
    int beforeDeclarationLine = token.line() - 1;
    SyntaxTrivia trivia = Iterables.getLast(token.trivias(), null);
    return trivia != null && beforeDeclarationLine == trivia.endLine() && isValuableComment(trivia);
  }

  private static boolean isClassAbstract(MethodDeclarationTree tree) {
    ClassDeclarationTree classTree = (ClassDeclarationTree) TreeUtils.findAncestorWithKind(tree, Collections.singletonList(Kind.CLASS_DECLARATION));
    return classTree != null && classTree.modifierToken() != null && classTree.modifierToken().text().equalsIgnoreCase("abstract");
  }

  private static boolean hasValuableBody(BlockTree tree) {
    if (!tree.statements().isEmpty()) {
      return true;
    }

    // Check whether there is a valuable comment in method body
    SyntaxTrivia trivia = Iterables.getLast(tree.closeCurlyBraceToken().trivias(), null);
    return trivia != null && isValuableComment(trivia);
  }

  private static boolean isValuableComment(SyntaxToken trivia) {
    return VALUABLE_COMMENT_PATTERN.matcher(trivia.text()).find();
  }

  private static boolean isConstructorPropertyPromotion(MethodDeclarationTree tree) {
    return tree.name().text().equalsIgnoreCase("__construct") && tree.parameters().parameters().stream().anyMatch(p -> p.visibility() != null);
  }

  private void commitIssue(FunctionTree tree, String type) {
    context().newIssue(this, tree, String.format(MESSAGE, type));
  }

}
