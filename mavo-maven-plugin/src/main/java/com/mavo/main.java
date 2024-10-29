package com.mavo;

import algorithm.POMAnalyzerAlgorithm;

public class main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String originalPomPath = "D:\\Anurag\\MAVO Workspace\\example";
		boolean excludeParentDependencies = true;
		boolean groupRecommendations = false;
		boolean optimizeRecommendations = false;
		String patchRestriction = "major";
		
        POMAnalyzerAlgorithm.pomAnalyzerAlgorithm(originalPomPath, excludeParentDependencies, 
        		groupRecommendations, optimizeRecommendations, patchRestriction);
	}

}
