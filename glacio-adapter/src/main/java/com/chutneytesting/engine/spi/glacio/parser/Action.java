package com.chutneytesting.engine.spi.glacio.parser;

import com.chutneytesting.engine.domain.environment.Target;
import java.util.Map;

public class Action {

    public final String type;
    public final Target target;
    public final Map<String, Object> inputs;
    public final Map<String, Object> outputs;

    public Action(String type, Target target, Map<String, Object> inputs, Map<String, Object> outputs) {
        this.type = type;
        this.target = target;
        this.inputs = inputs;
        this.outputs = outputs;
    }

}
