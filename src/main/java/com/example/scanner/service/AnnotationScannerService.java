package com.example.scanner.service;

import com.example.scanner.model.ClassInfo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnnotationScannerService {

    private final MetadataReaderFactory metadataReaderFactory;
    private final PathMatchingResourcePatternResolver resourcePatternResolver;

    public AnnotationScannerService() {
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
        this.metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);
    }

    public List<ClassInfo> findClassesWithAnnotation(String annotationName) {
        List<ClassInfo> results = new ArrayList<>();
        if (annotationName == null || annotationName.trim().isEmpty()) {
            return results;
        }

        try {
            // D'abord essayer de charger directement l'annotation
            Class<? extends Annotation> targetAnnotation = loadAnnotationClass(annotationName);
            
            if (targetAnnotation == null) {
                targetAnnotation = getStandardAnnotation(annotationName);
            }

            if (targetAnnotation != null) {
                // Créer une copie finale pour l'utiliser dans le lambda
                final Class<? extends Annotation> finalTargetAnnotation = targetAnnotation;
                
                // Scanner le classpath pour les classes annotées
                String packageSearchPath = "classpath*:com/example/**/*.class";
                Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
                
                System.out.println("Scanning " + resources.length + " classes in classpath...");
                
                for (Resource resource : resources) {
                    try {
                        if (resource.isReadable()) {
                            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                            AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
                            
                            if (metadata.hasAnnotation(finalTargetAnnotation.getName())) {
                                Class<?> clazz = Class.forName(metadata.getClassName());
                                
                                ClassInfo classInfo = new ClassInfo();
                                classInfo.setClassName(clazz.getSimpleName());
                                classInfo.setPackageName(clazz.getPackage() != null ? clazz.getPackage().getName() : "");
                                classInfo.setAnnotationName(finalTargetAnnotation.getName());
                                
                                // Récupérer les méthodes annotées
                                List<String> methods = Arrays.stream(clazz.getDeclaredMethods())
                                        .filter(method -> Arrays.stream(method.getAnnotations())
                                                .anyMatch(ann -> ann.annotationType().equals(finalTargetAnnotation)))
                                        .map(Method::getName)
                                        .collect(Collectors.toList());
                                
                                classInfo.setMethods(methods);
                                results.add(classInfo);
                                
                                System.out.println("Found annotated class: " + clazz.getName());
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing class: " + resource.getDescription());
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error during annotation scanning: ");
            e.printStackTrace();
        }
        
        return results;
    }
    
    private Class<? extends Annotation> loadAnnotationClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotation()) {
                return (Class<? extends Annotation>) clazz;
            }
        } catch (ClassNotFoundException e) {
            // L'annotation n'existe pas, on essaie avec le package complet
            if (!className.startsWith("org.springframework")) {
                try {
                    Class<?> clazz = Class.forName("org.springframework.stereotype." + className);
                    if (clazz.isAnnotation()) {
                        return (Class<? extends Annotation>) clazz;
                    }
                } catch (ClassNotFoundException ex) {
                    // Ignorer et retourner null
                }
            }
        }
        return null;
    }
    
    private Class<? extends Annotation> getStandardAnnotation(String keyword) {
        Map<String, Class<? extends Annotation>> standardAnnotations = new HashMap<>();
        standardAnnotations.put("controller", org.springframework.stereotype.Controller.class);
        standardAnnotations.put("restcontroller", org.springframework.web.bind.annotation.RestController.class);
        standardAnnotations.put("service", org.springframework.stereotype.Service.class);
        standardAnnotations.put("repository", org.springframework.stereotype.Repository.class);
        standardAnnotations.put("component", org.springframework.stereotype.Component.class);
        standardAnnotations.put("getmapping", org.springframework.web.bind.annotation.GetMapping.class);
        standardAnnotations.put("postmapping", org.springframework.web.bind.annotation.PostMapping.class);
        standardAnnotations.put("putmapping", org.springframework.web.bind.annotation.PutMapping.class);
        standardAnnotations.put("deletemapping", org.springframework.web.bind.annotation.DeleteMapping.class);
        standardAnnotations.put("requestmapping", org.springframework.web.bind.annotation.RequestMapping.class);
        
        return standardAnnotations.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains(keyword.toLowerCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
    
}
