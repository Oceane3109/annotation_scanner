package com.example.scanner.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClassInfo {
    private String packageName;
    private String className;
    private String annotationName;
    private List<MethodInfo> annotatedMethods = new ArrayList<>();
    
    @Data
    public static class MethodInfo {
        private String name;
        private String returnType;
        private List<String> annotations = new ArrayList<>();
        private boolean hasAnnotation;
        
        public void addAnnotation(String annotation) {
            this.annotations.add(annotation);
            this.hasAnnotation = true;
        }
        public List<String> getAnnotations() { return annotations; }
        public void setAnnotations(List<String> annotations) { this.annotations = annotations; }
    }
    
    public List<MethodInfo> getAnnotatedMethods() { return annotatedMethods; }
    public void setAnnotatedMethods(List<MethodInfo> methods) { this.annotatedMethods = methods; }
    public void addAnnotatedMethod(MethodInfo method) { this.annotatedMethods.add(method); }
    
    public String getFullClassName() {
        return packageName + "." + className;
    }
}
