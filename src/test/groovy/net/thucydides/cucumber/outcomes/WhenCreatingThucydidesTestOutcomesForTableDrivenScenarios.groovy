package net.thucydides.cucumber.outcomes

import com.github.goldin.spock.extensions.tempdir.TempDir
import net.thucydides.core.model.TestOutcome
import net.thucydides.core.model.TestResult
import net.thucydides.core.model.TestStep
import net.thucydides.core.model.TestTag
import net.thucydides.core.reports.OutcomeFormat
import net.thucydides.core.reports.TestOutcomeLoader
import net.thucydides.cucumber.integration.FailingScenario
import net.thucydides.cucumber.integration.MultipleScenarios
import net.thucydides.cucumber.integration.PendingScenario
import net.thucydides.cucumber.integration.SimpleScenario
import net.thucydides.cucumber.integration.SimpleTableScenario
import spock.lang.Specification

import static net.thucydides.core.model.TestResult.FAILURE
import static net.thucydides.core.model.TestResult.PENDING
import static net.thucydides.core.model.TestResult.SUCCESS
import static net.thucydides.cucumber.util.CucumberRunner.thucydidesRunnerForCucumberTestRunner

/**
 * Created by john on 23/07/2014.
 */
class WhenCreatingThucydidesTestOutcomesForTableDrivenScenarios extends Specification {

    @TempDir
    File outputDirectory

    /*
          Scenario Outline: Buying lots of widgets
            Given I want to purchase <amount> widgets
            And a widget costs $<cost>
            When I buy the widgets
            Then I should be billed $<total>
          Examples:
          | amount | cost | total |
          | 0      | 10   | 0     |
          | 1      | 10   | 10    |
          | 2      | 10   | 20    |
          | 2      | 0    | 0     |
     */
    def "should run table-driven scenarios successfully"() {
        given:
        def runtime = thucydidesRunnerForCucumberTestRunner(SimpleTableScenario.class, outputDirectory);

        when:
        runtime.run();
        def recordedTestOutcomes = new TestOutcomeLoader().forFormat(OutcomeFormat.JSON).loadFrom(outputDirectory);
        def testOutcome = recordedTestOutcomes[0]

        then:
        testOutcome.title == "Buying lots of widgets"

        and: "there should be one step for each row in the table"
        testOutcome.stepCount == 5

        and: "each of these steps should contain the scenario steps as children"
        def childSteps = testOutcome.testSteps[0].children.collect { step -> step.description }
        childSteps == ['Given I want to purchase 0 widgets', 'And a widget costs $10', 'When I buy the widgets', 'Then I should be billed $0']

        and:
        testOutcome.dataTable.rows.collect { it.result } == [SUCCESS, SUCCESS, SUCCESS, SUCCESS, SUCCESS]

        and:
        testOutcome.exampleFields == ["amount", "cost","total"]
        testOutcome.dataTable.rows[0].stringValues == ["0","10","0"]
        testOutcome.dataTable.rows[1].stringValues == ["1","10","10"]
    }


}