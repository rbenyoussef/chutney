package com.chutneytesting.engine.infrastructure.delegation;

import static com.chutneytesting.engine.api.execution.HttpTestEngine.EXECUTION_URL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.StepExecutionReportDto;
import com.chutneytesting.engine.domain.delegation.CannotDelegateException;
import com.chutneytesting.engine.domain.delegation.ConnectionChecker;
import com.chutneytesting.engine.domain.delegation.DelegationClient;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.report.StepExecutionReport;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/* TODO all -
    An agent receiving a scenario fragment with a finally action will execute that teardown as soon as it finnish.
    The complete scenario will not work due to this early unexpected teardown.
    Thus, Finally Actions should be driven by the main Agent executing the whole scenario.
*/
public class HttpClient implements DelegationClient {

    private final RestTemplate restTemplate;
    private final ConnectionChecker connectionChecker;

    public HttpClient() {
        this.restTemplate = new RestTemplate();
        this.connectionChecker = new TcpConnectionChecker();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.findAndRegisterModules();

        restTemplate.setMessageConverters(Lists.newArrayList(new MappingJackson2HttpMessageConverter(objectMapper)));
    }

    @Override
    public StepExecutionReport handDown(StepDefinition stepDefinition, NamedHostAndPort delegate) throws CannotDelegateException {
        if (connectionChecker.canConnectTo(delegate)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ExecutionRequestDto> request = new HttpEntity<>(ExecutionRequestMapper.from(stepDefinition), headers);
            StepExecutionReportDto reportDto = restTemplate.postForObject("https://" + delegate.host() + ":" + delegate.port() + EXECUTION_URL, request, StepExecutionReportDto.class);
            return StepExecutionReportMapper.fromDto(reportDto);
        } else {
            throw new CannotDelegateException("Unable to connect to " + delegate.name() + " at " + delegate.host() + ":" + delegate.port());
        }
    }
}
