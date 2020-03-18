package com.chutneytesting.engine.api.glacio;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import com.chutneytesting.engine.domain.environment.NoTarget;
import com.chutneytesting.engine.spi.glacio.parser.Action;
import com.chutneytesting.engine.spi.glacio.parser.SentenceParser;
import java.util.List;
import java.util.Map;

public class SuccessParser implements SentenceParser {

    @Override
    public Map<String, List<String>> keywords() {
        return singletonMap("fr", singletonList("success"));
    }

    @Override
    public Action parse(String sentence) {
        return new Action("success", NoTarget.NO_TARGET, emptyMap(), emptyMap());
    }

}
