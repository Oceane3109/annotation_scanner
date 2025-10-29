package com.example.scanner.model;

import lombok.Data;
import java.util.List;

@Data
public class ClassInfo {
    private String className;
    private String packageName;
    private String annotationName;
    private List<String> methods;
    
    public String getFullClassName() {
        return packageName + "." + className;
    }
}
