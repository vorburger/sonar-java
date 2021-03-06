/*
 * Java :: IT :: Plugin :: Tests
 * Copyright (C) 2013 SonarSource
 * dev@sonar.codehaus.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.it.java.suite;

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.MavenBuild;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueClient;
import org.sonar.wsclient.issue.IssueQuery;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class SquidTest {

  @ClassRule
  public static Orchestrator orchestrator = JavaTestSuite.ORCHESTRATOR;

  @BeforeClass
  public static void init() {
    orchestrator.resetData();

    MavenBuild build = MavenBuild.create(TestUtils.projectPom("squid"))
      .setCleanPackageSonarGoals()
      .setProperty("sonar.scm.disabled", "true")
      .setProperty("sonar.profile", "squid");
    orchestrator.executeBuild(build);
  }

  /**
   * SONAR-2333
   */
  @Test
  public void should_detect_cycles() {
    IssueClient issueClient = orchestrator.getServer().wsClient().issueClient();
    List<Issue> issues = issueClient.find(IssueQuery.create().components(JavaTestSuite.keyFor("com.sonarsource.it.samples:squid", "package1/", "Class1.java"))).list();
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).ruleKey()).isEqualTo("squid:CycleBetweenPackages");
    assertThat(issues.get(0).line()).isNull();
  }

  @Test
  public void should_detect_missing_package_info() throws Exception {
    IssueClient issueClient = orchestrator.getServer().wsClient().issueClient();
    List<Issue> issues = issueClient.find(IssueQuery.create().components(JavaTestSuite.keyFor("com.sonarsource.it.samples:squid", "package1", ""))).list();
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).ruleKey()).isEqualTo("squid:S1228");
    assertThat(issues.get(0).line()).isNull();
    issues = issueClient.find(IssueQuery.create().components("com.sonarsource.it.samples:squid:src/test/java/package1")).list();
    assertThat(issues).isEmpty();
  }

  @Test
  public void should_not_fail_on_bytecode_visitor_issue_on_file() throws Exception {
    IssueClient issueClient = orchestrator.getServer().wsClient().issueClient();
    List<Issue> issues = issueClient.find(IssueQuery.create().components(JavaTestSuite.keyFor("com.sonarsource.it.samples:squid", "package2/", "Class2.java"))).list();
    assertThat(issues).hasSize(1);
    assertThat(issues.get(0).ruleKey()).isEqualTo("squid:UnusedPrivateMethod");
    assertThat(issues.get(0).line()).isNull();
  }
}
