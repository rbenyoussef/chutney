package com.chutneytesting.engine.spi.glacio.parser;

import java.util.List;
import java.util.Map;

public interface SentenceParser {

    Map<String, List<String>> keywords();

    Action parse(String sentence);

}
