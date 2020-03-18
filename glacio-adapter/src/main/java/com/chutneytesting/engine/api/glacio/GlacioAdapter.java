package com.chutneytesting.engine.api.glacio;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.spi.glacio.parser.Action;
import com.chutneytesting.engine.spi.glacio.parser.ActionParserRegistry;
import com.chutneytesting.engine.spi.glacio.parser.SentenceParser;
import com.github.fridujo.glacio.ast.Feature;
import com.github.fridujo.glacio.ast.RootStep;
import com.github.fridujo.glacio.ast.Scenario;
import com.github.fridujo.glacio.ast.Step;
import com.github.fridujo.glacio.parsing.charstream.CharStream;
import com.github.fridujo.glacio.parsing.i18n.GherkinLanguages;
import com.github.fridujo.glacio.parsing.lexer.Lexer;
import com.github.fridujo.glacio.parsing.parser.AstParser;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GlacioAdapter {

    private final ActionParserRegistry actionParserRegistry;

    public GlacioAdapter(ActionParserRegistry actionParserRegistry) {
        this.actionParserRegistry = actionParserRegistry;
    }

    public List<StepDefinition> toChutneyStepDefinition(String text) {
        Feature feature = this.toFeature(text);
        List<Scenario> scenarios = feature.getScenarios();

        return scenarios.stream()
            .map(s -> toStepDefinition("fr", feature.getName(), s))
            .collect(Collectors.toList());
    }

    private Feature toFeature(String text) {
        Lexer lexer = new Lexer(new CharStream(text));
        AstParser astParser = new AstParser(lexer, GherkinLanguages.load());
        return astParser.parseFeature();
    }

    private StepDefinition toStepDefinition(String local, String featureName, Scenario scenario) {
        String name = featureName + " - " + scenario.getName();

        List<StepDefinition> subSteps = scenario.getSteps().stream()
            .map(s -> toStepDefinition(local, s))
            .collect(Collectors.toList());

        return new StepDefinition(name, null, "", null, emptyMap(), subSteps, emptyMap());
    }

    private StepDefinition toStepDefinition(String local, RootStep rootStep) {
        String name = rootStep.getKeyword().getLiteral() + rootStep.getText();

        List<StepDefinition> subSteps = rootStep.getSubsteps().stream()
            .map(step -> toStepDefinition(local, step))
            .collect(Collectors.toList());

        return new StepDefinition(name, null, "", null, emptyMap(), subSteps, emptyMap());
    }

    private StepDefinition toStepDefinition(String local, Step step) {
        if (isLeaf(step)) {
            return parseAction(local, step);
        }

        List<StepDefinition> subSteps = step.getSubsteps().stream()
            .map(subStep -> toStepDefinition(local, subStep))
            .collect(Collectors.toList());

        return new StepDefinition(step.getText(), null, "", null, emptyMap(), subSteps, emptyMap());
    }

    private static boolean isLeaf(Step step) {
        return step.getSubsteps().isEmpty();
    }

    private static final String ACTION_STEP_REGEX = "^(?<trigger>\\w+):(\\W+)(?<action>\\w+)(?<sentence>.*)$";
    private static final Pattern ACTION_STEP_PATTERN = Pattern.compile(ACTION_STEP_REGEX);

    private StepDefinition parseAction(String local, Step step) {
        Matcher matcher = ACTION_STEP_PATTERN.matcher(step.getText());

        if (matcher.matches()) {
            String trigger = matcher.group("trigger");
            if (isKnownTrigger(trigger)) {
                String actionName = matcher.group("action");
                String sentence = matcher.group("sentence");

                Optional<SentenceParser> parser = findParser(local, actionName);
                return parser
                    .map(p -> toStepDefinition(p.parse(sentence)))
                    .orElse(new StepDefinition(step.getText(), null, "", null, emptyMap(), emptyList(), emptyMap()));
            }
        }

        return new StepDefinition(step.getText(), null, "", null, emptyMap(), emptyList(), emptyMap());
    }

    private StepDefinition toStepDefinition(Action action) {
        return new StepDefinition("Doing " + action.type + " action", null /* TODO - Target */, action.type, null /* TODO - Strategy */, action.inputs, emptyList(), action.outputs);
    }

    private static boolean isKnownTrigger(String trigger) {
        return "Do".equals(trigger);
    }

    private Optional<SentenceParser> findParser(String local, String actionName) {
        return actionParserRegistry.findBy(local, actionName);
    }

}
