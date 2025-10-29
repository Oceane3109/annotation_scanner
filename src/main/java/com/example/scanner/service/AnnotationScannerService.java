package com.example.scanner.service;

import com.example.scanner.model.ClassInfo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
        String targetAnnotation = annotationName.startsWith("@") ? 
                                annotationName.substring(1) : annotationName;

        try {
            // Récupérer toutes les classes du package de base
            String basePackage = "com.example.scanner";
            String packageSearchPath = "classpath*:" + basePackage.replace('.', '/') + "/**/*.class";
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            
            for (Resource resource : resources) {
                try {
                    if (resource.isReadable()) {
                        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
                        
                        // Vérifier si la classe a l'annotation recherchée
                        boolean hasTargetAnnotation = metadata.getAnnotationTypes().stream()
                            .anyMatch(ann -> ann.equals(targetAnnotation) || 
                                          ann.endsWith("." + targetAnnotation) ||
                                          ann.endsWith("$" + targetAnnotation));
                        
                        if (hasTargetAnnotation) {
                            Class<?> clazz = Class.forName(metadata.getClassName());
                            ClassInfo classInfo = new ClassInfo();
                            classInfo.setClassName(clazz.getSimpleName());
                            classInfo.setPackageName(clazz.getPackage() != null ? clazz.getPackage().getName() : "");
                            classInfo.setAnnotationName("@" + targetAnnotation);
                            
                            // Ajouter toutes les méthodes de la classe
                            for (Method method : clazz.getDeclaredMethods()) {
                                ClassInfo.MethodInfo methodInfo = new ClassInfo.MethodInfo();
                                methodInfo.setName(method.getName());
                                methodInfo.setReturnType(method.getReturnType().getSimpleName());
                                
                                // Ajouter toutes les annotations de la méthode
                                for (Annotation ann : method.getAnnotations()) {
                                    String simpleName = ann.annotationType().getSimpleName();
                                    if (!simpleName.equals("RequestMapping") && 
                                        !ann.annotationType().isAnnotationPresent(RestController.class)) {
                                        methodInfo.addAnnotation(simpleName);
                                    }
                                }
                                
                                classInfo.addAnnotatedMethod(methodInfo);
                                
                                // Afficher les méthodes avec leurs annotations pour le débogage
                                if (!methodInfo.getAnnotations().isEmpty()) {
                                    System.out.println(" - " + methodInfo.getName() + "() : " + 
                                        methodInfo.getAnnotations().stream()
                                            .map(a -> "@" + a)
                                            .collect(Collectors.joining(", ")));
                                }
                            }
                            
                            results.add(classInfo);
                            System.out.println("Found class: " + clazz.getName());
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Could not load class: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error processing class: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error scanning for classes: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * Gets a standard annotation class by simple name
     * @param annotationName The simple name of the annotation (e.g., "GetMapping")
     * @return The annotation class or null if not found
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Annotation> getAnnotationClass(String annotationName) {
        // Try to load the annotation class directly
        try {
            return (Class<? extends Annotation>) Class.forName(annotationName);
        } catch (ClassNotFoundException e) {
            // If the class is not found with the exact name, try with common packages
            String[] commonPackages = {
                "",
                "org.springframework.web.bind.annotation.",
                "org.springframework.stereotype.",
                "org.springframework.context.annotation.",
                "javax.persistence.",
                "jakarta.persistence.",
                "java.lang.",
                "org.springframework.messaging.handler.annotation.",
                "org.springframework.web.bind.annotation.rest.",
                "org.springframework.web.bind.annotation.mapping."
            };
            
            for (String pkg : commonPackages) {
                try {
                    return (Class<? extends Annotation>) Class.forName(pkg + 
                        (annotationName.startsWith("@") ? annotationName.substring(1) : annotationName));
                } catch (ClassNotFoundException ignored) {
                    // Try next package
                }
            }
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends Annotation> getStandardAnnotation(String annotationName) {
        // Map of common annotation simple names to their full class names
        Map<String, String> standardAnnotations = new HashMap<>();
        standardAnnotations.put("GetMapping", "org.springframework.web.bind.annotation.GetMapping");
        standardAnnotations.put("PostMapping", "org.springframework.web.bind.annotation.PostMapping");
        standardAnnotations.put("PutMapping", "org.springframework.web.bind.annotation.PutMapping");
        standardAnnotations.put("DeleteMapping", "org.springframework.web.bind.annotation.DeleteMapping");
        standardAnnotations.put("PatchMapping", "org.springframework.web.bind.annotation.PatchMapping");
        standardAnnotations.put("RequestMapping", "org.springframework.web.bind.annotation.RequestMapping");
        standardAnnotations.put("RestController", "org.springframework.web.bind.annotation.RestController");
        standardAnnotations.put("Controller", "org.springframework.stereotype.Controller");
        standardAnnotations.put("Service", "org.springframework.stereotype.Service");
        standardAnnotations.put("Repository", "org.springframework.stereotype.Repository");
        standardAnnotations.put("Component", "org.springframework.stereotype.Component");
        standardAnnotations.put("Autowired", "org.springframework.beans.factory.annotation.Autowired");
        standardAnnotations.put("Value", "org.springframework.beans.factory.annotation.Value");
        standardAnnotations.put("Configuration", "org.springframework.context.annotation.Configuration");
        standardAnnotations.put("Bean", "org.springframework.context.annotation.Bean");
        standardAnnotations.put("Override", "java.lang.Override");
        standardAnnotations.put("Deprecated", "java.lang.Deprecated");
        standardAnnotations.put("SuppressWarnings", "java.lang.SuppressWarnings");
        
        String fullName = standardAnnotations.get(annotationName);
        if (fullName != null) {
            try {
                return (Class<? extends Annotation>) Class.forName(fullName);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Finds all annotations used in the project
     * @return Set of unique annotation names
     */
    public Set<String> findAllAnnotations() {
        Set<String> allAnnotations = new HashSet<>();
        
        try {
            // Scanner le classpath pour toutes les classes
            String packageSearchPath = "classpath*:com/example/**/*.class";
            Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
            
            for (Resource resource : resources) {
                try {
                    if (resource.isReadable()) {
                        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                        AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
                        
                        // Ajouter les annotations de la classe
                        allAnnotations.addAll(metadata.getAnnotationTypes());
                        
                        // Obtenir les méthodes et leurs annotations
                        Class<?> clazz = Class.forName(metadata.getClassName());
                        for (Method method : clazz.getDeclaredMethods()) {
                            for (Annotation annotation : method.getAnnotations()) {
                                allAnnotations.add(annotation.annotationType().getName());
                            }
                        }
                        
                        // Obtenir les annotations des champs
                        for (Field field : clazz.getDeclaredFields()) {
                            for (Annotation annotation : field.getAnnotations()) {
                                allAnnotations.add(annotation.annotationType().getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing class: " + resource.getDescription());
                }
            }
        } catch (IOException e) {
            System.err.println("Error scanning for annotations: " + e.getMessage());
        }
        
        return allAnnotations;
    }
}
