package eci.arep.certificate.controller;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "https://santiagoarep.ddns.net", allowCredentials = "true")
@RestController
@RequestMapping("/api")
public class ServiceController {

    @GetMapping("/")
    public String greeting() {
        return "Hello, World!";
    }

    // Endpoint con parámetros en la URL
    @GetMapping("/greeting/{name}")
    public String greeting(@PathVariable String name) {
        return "Hello, " + name + "!";
    }

}
