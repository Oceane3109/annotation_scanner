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
    
    public static class MethodInfo {
        private String name;
        private String returnType;
        private List<String> annotations = new ArrayList<>();
        
        // Getters et Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getReturnType() { return returnType; }
        public void setReturnType(String returnType) { this.returnType = returnType; }
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
