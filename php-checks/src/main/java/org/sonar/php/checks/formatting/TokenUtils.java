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
package org.sonar.php.checks.formatting;

import org.sonar.api.utils.Preconditions;
import org.sonar.php.api.PHPPunctuator;
import org.sonar.plugins.php.api.tree.lexical.SyntaxToken;

public class TokenUtils {

  private TokenUtils() {
  }

  public static String buildIssueMsg(int nbSpace, String end) {
    return (new StringBuilder()).append("Put ")
      .append(nbSpace > 1 ? "only " : "")
      .append("one space ")
      .append(end).toString();
  }

  /**
   * Return true if the given token is one of the given types.
   */
  public static boolean isType(SyntaxToken token, PHPPunctuator... types) {
    boolean isOneOfType = false;
    for (PHPPunctuator type : types) {
      isOneOfType |= type.getValue().equals(token.text());
    }
    return isOneOfType;
  }

  /**
   * Returns true if all the tokens given as parameters are on the same line.
   */
  public static boolean isOnSameLine(SyntaxToken... tokens) {
    Preconditions.checkArgument(tokens.length > 0);

    int lineRef = tokens[0].line();
    for (SyntaxToken token : tokens) {
      if (token.line() != lineRef) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns number of space between the 2 tokens.
   */
  protected static int getNbSpaceBetween(SyntaxToken token1, SyntaxToken token2) {
    int token1EndColumn = token1.column() + (token1.text().length() - 1);
    int tok2StartColumn = token2.column();

    return tok2StartColumn - token1EndColumn - 1;
  }

}
