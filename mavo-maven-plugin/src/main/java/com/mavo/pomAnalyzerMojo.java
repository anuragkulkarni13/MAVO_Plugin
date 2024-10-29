package com.mavo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import algorithm.POMAnalyzerAlgorithm;

@Mojo(name = "pom-analyzer", defaultPhase = LifecyclePhase.COMPILE)
public class pomAnalyzerMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "excludeParentDependencies", defaultValue="true")
    boolean excludeParentDependencies;
    
    @Parameter(property = "groupRecommendations", defaultValue="true")
    boolean groupRecommendations;
    
    @Parameter(property = "optimizeRecommendations", defaultValue="true")
    boolean optimizeRecommendations;
    
    @Parameter(property = "patchRestriction", defaultValue="")
    String patchRestriction;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File file = project.getBasedir();
        String originalPomPath = file.getAbsolutePath();
        getLog().info("location - "+originalPomPath);
        
        if (patchRestriction == null || patchRestriction.isEmpty()) {
            patchRestriction = "";
        }
        
        POMAnalyzerAlgorithm.pomAnalyzerAlgorithm(originalPomPath, excludeParentDependencies, 
        		groupRecommendations, optimizeRecommendations, patchRestriction);
    }
}