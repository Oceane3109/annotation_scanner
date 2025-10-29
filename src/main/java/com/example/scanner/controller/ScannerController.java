package com.example.scanner.controller;

import com.example.scanner.model.ClassInfo;
import com.example.scanner.service.AnnotationScannerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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
    public String scan(@RequestParam("annotation") String annotationName, Model model) {
        List<ClassInfo> results = scannerService.findClassesWithAnnotation(annotationName);
        model.addAttribute("results", results);
        model.addAttribute("searchedAnnotation", annotationName);
        
        // Ajouter la liste de toutes les annotations disponibles
        Set<String> allAnnotations = scannerService.findAllAnnotations();
        model.addAttribute("allAnnotations", allAnnotations);
        
        return "results";
    }
    
    @GetMapping("/all-annotations")
    public String getAllAnnotations(Model model) {
        Set<String> allAnnotations = scannerService.findAllAnnotations();
        model.addAttribute("allAnnotations", allAnnotations);
        return "all-annotations";
    }
}
