package com.chutneytesting.documentation.api;

import com.chutneytesting.documentation.infra.ExamplesRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documentation")
public class DocumentationController {

    private final ExamplesRepository examplesRepository;

    public DocumentationController(ExamplesRepository examplesRepository) {
        this.examplesRepository = examplesRepository;
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean getActivationStatus() {
        return examplesRepository.isActive();
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean toggleActivationStatus() {
        return examplesRepository.toggleActivation();
    }
}
