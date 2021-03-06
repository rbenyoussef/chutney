package com.chutneytesting.task.http;

import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import com.chutneytesting.task.spi.injectable.Target;
import com.chutneytesting.task.spi.time.Duration;
import com.chutneytesting.task.http.domain.HttpClient;
import com.chutneytesting.task.http.domain.HttpClientFactory;
import com.chutneytesting.task.http.domain.HttpTask;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class HttpPostTask implements Task {

    private static final long DEFAULT_TIMEOUT = 2000;

    private final Target target;
    private final Logger logger;
    private final String uri;
    private final Map<String, String> headers;
    private final Object body;
    private final int timeout;

    public HttpPostTask(Target target, Logger logger, @Input("uri") String uri, @Input("body") String body, @Input("headers") Map<String, String> headers, @Input("timeout") String timeout) {
        this.target = target;
        this.logger = logger;
        this.uri = uri;
        this.body = body;
        this.headers = headers != null ? headers : new HashMap<>();
        this.timeout = ofNullable(timeout)
            .filter(StringUtils::isNotBlank)
            .map(Duration::parse)
            .map(Duration::toMilliseconds)
            .orElse(DEFAULT_TIMEOUT).intValue();
    }

    @Override
    public TaskExecutionResult execute() {
        HttpClient httpClient = new HttpClientFactory().create(target, String.class, timeout);
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((key, value) -> httpHeaders.add(key, value));
        Supplier<ResponseEntity<String>> caller = () -> httpClient.post(this.uri, ofNullable(body).orElse("{}"), httpHeaders);
        return HttpTask.httpCall(logger, caller);
    }
}
