package com.chutneytesting.engine;

import static com.chutneytesting.tools.Streams.identity;

import com.chutneytesting.engine.spi.glacio.parser.ActionParserRegistry;
import com.chutneytesting.engine.spi.glacio.parser.SentenceParser;
import com.chutneytesting.tools.ThrowingFunction;
import com.chutneytesting.tools.loader.ExtensionLoaders;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlacioSpringConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlacioSpringConfiguration.class);

    @Bean
    Set<SentenceParser> stepExecutionStrategies() {
        return ExtensionLoaders
            .classpathToClass("META-INF/extension/chutney.parsers")
            .load()
            .stream()
            .map(ThrowingFunction.toUnchecked(GlacioSpringConfiguration::<SentenceParser>instantiate))
            .map(identity(c -> LOGGER.debug("Loading parser: (" + c.getClass().getSimpleName() + ")")))
            .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private static <T> T instantiate(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        return (T) clazz.newInstance();
    }

    @Bean
    ActionParserRegistry stepExecutionStrategyResolver(Set<SentenceParser> parsers) {
        return new ActionParserRegistry(parsers);
    }

}
