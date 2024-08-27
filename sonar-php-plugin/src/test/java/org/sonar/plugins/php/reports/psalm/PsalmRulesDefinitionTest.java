/*
 * SonarQube PHP Plugin
 * Copyright (C) 2010-2024 SonarSource SA
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
package org.sonar.plugins.php.reports.psalm;

import java.util.Map;
import javax.annotation.Nullable;
import org.sonar.api.SonarRuntime;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.rules.CleanCodeAttribute;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.php.reports.AbstractExternalRulesDefinition;
import org.sonar.plugins.php.reports.AbstractExternalRulesDefinitionTest;

import static org.assertj.core.api.Assertions.assertThat;

class PsalmRulesDefinitionTest extends AbstractExternalRulesDefinitionTest {

  @Override
  protected void customRuleAssertion(RulesDefinition.Repository repository, boolean shouldSupportCCT) {
    RulesDefinition.Rule rule = repository.rule("AbstractMethodCall");
    assertThat(rule).isNotNull();
    assertThat(rule.name()).isEqualTo("AbstractMethodCall");
    assertThat(rule.tags()).isEmpty();

    if (shouldSupportCCT) {
      assertThat(rule.cleanCodeAttribute()).isEqualTo(CleanCodeAttribute.LOGICAL);
      assertThat(rule.defaultImpacts()).containsOnly(Map.entry(SoftwareQuality.RELIABILITY, Severity.HIGH));
    } else {
      assertThat(rule.cleanCodeAttribute()).isNull();
      assertThat(rule.defaultImpacts()).containsOnly(Map.entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
    }
  }

  @Override
  protected AbstractExternalRulesDefinition rulesDefinition(@Nullable SonarRuntime sonarRuntime) {
    return new PsalmRulesDefinition(sonarRuntime);
  }

  @Override
  protected int numberOfRules() {
    return 266;
  }

  @Override
  protected String reportName() {
    return PsalmSensor.PSALM_REPORT_NAME;
  }

  @Override
  protected String reportKey() {
    return PsalmSensor.PSALM_REPORT_KEY;
  }
}
