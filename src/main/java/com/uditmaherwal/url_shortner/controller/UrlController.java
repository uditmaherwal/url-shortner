package com.uditmaherwal.url_shortner.controller;

import com.uditmaherwal.url_shortner.service.UrlService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urlShortner")
@RequiredArgsConstructor
public class UrlController {

    private final UrlService urlService;

    @RequestMapping(value = "/create/{url}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity shortenUrl(@PathVariable String url) {
        String shortUrlEntry = urlService.getShortUrl(url);
        return ResponseEntity.ok(shortUrlEntry);
    }

    @RequestMapping(value = "/{key}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getUrl(@PathVariable String key) {
        String url = urlService.redirect(key);
        if(url.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(302).header("Location", url).build();
    }
}
