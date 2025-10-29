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
    
    private Class<? extends Annotation> getStandardAnnotation(String simpleName) {
        // Mapping des noms courts d'annotations standards vers leurs classes
        Map<String, Class<? extends Annotation>> standardAnnotations = new HashMap<>();
        standardAnnotations.put("Controller", org.springframework.stereotype.Controller.class);
        standardAnnotations.put("Service", org.springframework.stereotype.Service.class);
        standardAnnotations.put("Repository", org.springframework.stereotype.Repository.class);
        standardAnnotations.put("Component", org.springframework.stereotype.Component.class);
        standardAnnotations.put("RestController", org.springframework.web.bind.annotation.RestController.class);
        standardAnnotations.put("GetMapping", org.springframework.web.bind.annotation.GetMapping.class);
        standardAnnotations.put("PostMapping", org.springframework.web.bind.annotation.PostMapping.class);
        standardAnnotations.put("RequestMapping", org.springframework.web.bind.annotation.RequestMapping.class);
        standardAnnotations.put("Autowired", org.springframework.beans.factory.annotation.Autowired.class);
        
        return standardAnnotations.get(simpleName);
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
