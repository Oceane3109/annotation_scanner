package com.example.scanner.controller;

import com.example.scanner.model.ClassInfo;
import com.example.scanner.service.AnnotationScannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ScannerController {

    private final AnnotationScannerService scannerService;

    public ScannerController(AnnotationScannerService scannerService) {
        this.scannerService = scannerService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("defaultAnnotation", "org.springframework.stereotype.Controller");
        return "index";
    }

    @PostMapping("/scan")
    public String scan(@RequestParam("annotationName") String annotationName, Model model) {
        List<ClassInfo> classes = scannerService.findClassesWithAnnotation(annotationName);
        model.addAttribute("classes", classes);
        model.addAttribute("annotationName", annotationName);
        return "results";
    }
}
