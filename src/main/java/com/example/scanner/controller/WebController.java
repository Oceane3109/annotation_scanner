package com.example.scanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web")
public class WebController {

    @GetMapping("/welcome")
    public String welcomePage(Model model) {
        model.addAttribute("message", "Bienvenue sur notre page web!");
        return "welcome";
    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="Visiteur") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
