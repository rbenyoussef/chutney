package com.chutneytesting.engine.spi.glacio.parser;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ActionParserRegistry {

    private final Set<SentenceParser> parsers;
    private final Map<String /*local*/, Map<String /*keyword*/, SentenceParser /*parser*/>> registry = new ConcurrentHashMap<>();

    public ActionParserRegistry() {
        this(new HashSet<>());
    }

    public ActionParserRegistry(Set<SentenceParser> parsers) {
        this.parsers = ofNullable(parsers).orElse(new HashSet<>());
    }

    private void buildRegistry(String local) {
        Map<String, SentenceParser> localizedKeywords = this.parsers.stream()
            .map(parser -> parser.keywords().get(local).stream()
                .collect(toMap(
                    Function.identity(),
                    s -> parser,
                    (x, y) -> x /* should never happen */,
                    ConcurrentHashMap::new
                )))
            .flatMap(map -> map.entrySet().stream())
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        registry.put(local, localizedKeywords);
    }

    public Optional<SentenceParser> findBy(String local, String actionName) {
        if (registry.get(local) == null) {
            buildRegistry(local);
        }

        return ofNullable(registry.get(local).get(actionName));
    }

}
