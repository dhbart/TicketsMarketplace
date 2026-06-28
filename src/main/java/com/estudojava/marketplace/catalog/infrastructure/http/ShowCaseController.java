package com.estudojava.marketplace.catalog.infrastructure.http;

import com.estudojava.marketplace.catalog.application.BrowseShowcaseUsecase;
import com.estudojava.marketplace.catalog.application.dto.EventOutput;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/showcase")
public class ShowCaseController {
    private final BrowseShowcaseUsecase browseShowcaseUsecase;

    public ShowCaseController(BrowseShowcaseUsecase browseShowcaseUsecase) {
        this.browseShowcaseUsecase = browseShowcaseUsecase;
    }

    @GetMapping
    List<EventOutput> browseShowcase() {
        return browseShowcaseUsecase.execute();
    }
}
