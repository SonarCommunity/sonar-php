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
package org.sonar.php.checks;

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.php.checks.utils.CheckUtils;
import org.sonar.plugins.php.api.cfg.CfgBlock;
import org.sonar.plugins.php.api.cfg.ControlFlowGraph;
import org.sonar.plugins.php.api.tree.Tree;
import org.sonar.plugins.php.api.tree.Tree.Kind;
import org.sonar.plugins.php.api.visitors.PHPSubscriptionCheck;
import org.sonar.plugins.php.api.visitors.PreciseIssue;

@Rule(key = CodeFollowingJumpStatementCheck.KEY)
public class CodeFollowingJumpStatementCheck extends PHPSubscriptionCheck {

  public static final String KEY = "S1763";
  private static final String MESSAGE = "Delete this unreachable code or refactor the code to make it reachable.";
  private static final String SECONDARY_MESSAGE = "Statement exiting the current code block.";

  @Override
  public List<Kind> nodesToVisit() {
    return new ArrayList<>(ControlFlowGraph.KINDS_WITH_CONTROL_FLOW);
  }

  @Override
  public void visitNode(Tree tree) {
    ControlFlowGraph cfg = ControlFlowGraph.build(tree, context());
    if (cfg != null) {
      checkCfg(cfg);
    }
  }

  private void checkCfg(ControlFlowGraph cfg) {
    for (CfgBlock cfgBlock : cfg.blocks()) {
      if (cfgBlock.predecessors().isEmpty() && !cfgBlock.equals(cfg.start()) && !cfgBlock.elements().isEmpty()) {
        Tree firstElement = cfgBlock.elements().get(0);
        if ((firstElement.is(Kind.BREAK_STATEMENT) && firstElement.getParent().is(Kind.CASE_CLAUSE, Kind.DEFAULT_CLAUSE))
          || CheckUtils.isClosingTag(firstElement)) {
          continue;
        }

        PreciseIssue issue = context().newIssue(this, firstElement, MESSAGE);
        cfg.blocks().stream()
          .filter(block -> cfgBlock.equals(block.syntacticSuccessor()))
          .forEach(block -> issue.secondary(block.elements().get(block.elements().size() - 1), SECONDARY_MESSAGE));
      }
    }
  }
}
