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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.io.IOException;
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
        }

        try {
            // D'abord essayer de charger directement l'annotation
            Class<? extends Annotation> targetAnnotations = loadAnnotationClass(annotationName);
            
            if (targetAnnotations == null) {
                targetAnnotations = getStandardAnnotation(annotationName);
            }

            // Recherche dans toutes les classes du package
    }
    
    /**
     * Trouve toutes les annotations utilisées dans le projet
     * @return Liste des noms d'annotations uniques
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
