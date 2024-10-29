package recommendations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cache.DependencyCache;
import common.Constants;
import common.Utils;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import common.dto.VulnerabilityDTO;
import pom.DependencyOperations;
import pom.PomOperations;
import vulnerability.VulnerabilityAnalyzer;

public class POMOptimization {
	
	public static Map<String, Integer> optimizePOMRecommendations(String originalPomPath, List<POMDependencyDTO> allDependencies, List<DependencyDTO> pomRecommendations, String tempPomFilePath, String tempPomFileDirectory, String tempPomDependencyCheckReportLocation)
	{
		Map<String, Integer> leastVulCountCombinationMap = new HashMap<>();
		int leastVulCount = 0;
		String leastVulCountCombination = "";
		boolean first = false;
		String key = "";
		try {
			String DB_URL_COMB = DependencyCache.get_DB_URL_COMB(originalPomPath);

			Connection connection = DriverManager.getConnection(DB_URL_COMB);
			List<List<Integer>> comb = Utils.combinations(pomRecommendations.size());
			
	        for(List<Integer> co : comb)
	        {
	        	key = "";
	            // Clear all dependencies first
	        	PomOperations.clearAllDependencies(tempPomFilePath);

	        	for(POMDependencyDTO pomDependency : allDependencies)
	    		{
		        	PomOperations.addDependency(tempPomFilePath, pomDependency.getGroupId(), pomDependency.getArtifactId(), pomDependency.getVersion());
	    		}

	        	for(Integer c : co)
	        	{
	            	DependencyDTO p = pomRecommendations.get(c);
	            	
	            	String groupId = p.getGroupId();
	            	String artifactId = p.getArtifactId();
	            	String version = p.getVersion();
	            	
	            	System.out.println(c+" - "+groupId+" - "+artifactId+" - "+version);
	                PomOperations.addDependency(tempPomFilePath, groupId, artifactId, version);

	        	}
	        	
        		List<POMDependencyDTO> deps = PomOperations.getDepndenciesFromPOM(tempPomFilePath);
        		for(POMDependencyDTO dep : deps)
        		{
        			if(key.equalsIgnoreCase(""))
        			{
        				key += dep.getGroupId()+"#"+dep.getArtifactId()+"#"+dep.getVersion();
        			}
        			else
        			{
        				key += ","+dep.getGroupId()+"#"+dep.getArtifactId()+"#"+dep.getVersion();
        			}
        		}
        		
	        	int vulCount = 0;
	        	if(DependencyCache.isKeyPresent(connection, key))
	        	{
	        		vulCount = Integer.parseInt(DependencyCache.getCache(connection, key));
	        	}else {
	        		DependencyOperations.generateDependencyCheckReportWithPath(tempPomFilePath, tempPomFileDirectory, tempPomDependencyCheckReportLocation);
					Map<String, List<VulnerabilityDTO>> vulnerabilityMap = VulnerabilityAnalyzer.getVulnerabilityList(tempPomDependencyCheckReportLocation);
					
					for (Map.Entry<String, List<VulnerabilityDTO>> entry : vulnerabilityMap.entrySet()) {
//					    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().size());
					    vulCount += entry.getValue().size();
					}
					System.out.println(vulCount);
					DependencyCache.putCache(connection, key, String.valueOf(vulCount));
	        	}
	        	
	        	if(first == false)
	        	{
	        		leastVulCount = vulCount;
	        		leastVulCountCombination = key;
	        		first = true;
	        	}
	        	else
	        	{
	        		if(vulCount<leastVulCount)
	        		{
	        			leastVulCount = vulCount;
	        			leastVulCountCombination = key;
	        		}
	        	}
			}
	        
	        leastVulCountCombinationMap.put(leastVulCountCombination, leastVulCount);
		}catch (Exception e) {
			System.out.println(e);
		}
		return leastVulCountCombinationMap;
	}
	
	
	public static Map<String, Integer> organizePOMRecommendations(String originalPomPath, List<POMDependencyDTO> allDependencies, List<DependencyDTO> pomRecommendations, String tempPomFilePath, String tempPomFileDirectory, String tempPomDependencyCheckReportLocation)
	{
		Map<String, Integer> leastVulCountCombinationMap = new HashMap<>();
		int leastVulCount = 0;
		String leastVulCountCombination = "";
		boolean first = false;
		String key = "";
		try {
			String DB_URL_COMB = DependencyCache.get_DB_URL_COMB(originalPomPath);

			Connection connection = DriverManager.getConnection(DB_URL_COMB);
			
        	key = "";
            // Clear all dependencies first
        	PomOperations.clearAllDependencies(tempPomFilePath);

        	for(POMDependencyDTO pomDependency : allDependencies)
    		{
	        	PomOperations.addDependency(tempPomFilePath, pomDependency.getGroupId(), pomDependency.getArtifactId(), pomDependency.getVersion());
    		}
        	
        	for(DependencyDTO p : pomRecommendations)
        	{
            	String groupId = p.getGroupId();
            	String artifactId = p.getArtifactId();
            	String version = p.getVersion();
            	
                PomOperations.addDependency(tempPomFilePath, groupId, artifactId, version);

        	}
        	
    		List<POMDependencyDTO> deps = PomOperations.getDepndenciesFromPOM(tempPomFilePath);
    		for(POMDependencyDTO dep : deps)
    		{
    			if(key.equalsIgnoreCase(""))
    			{
    				key += dep.getGroupId()+"#"+dep.getArtifactId()+"#"+dep.getVersion();
    			}
    			else
    			{
    				key += ","+dep.getGroupId()+"#"+dep.getArtifactId()+"#"+dep.getVersion();
    			}
    		}
    		
        	int vulCount = 0;
        	if(DependencyCache.isKeyPresent(connection, key))
        	{
        		vulCount = Integer.parseInt(DependencyCache.getCache(connection, key));
        	}else {
        		DependencyOperations.generateDependencyCheckReportWithPath(tempPomFilePath, tempPomFileDirectory, tempPomDependencyCheckReportLocation);
				Map<String, List<VulnerabilityDTO>> vulnerabilityMap = VulnerabilityAnalyzer.getVulnerabilityList(tempPomDependencyCheckReportLocation);
				
				for (Map.Entry<String, List<VulnerabilityDTO>> entry : vulnerabilityMap.entrySet()) {
//					    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().size());
				    vulCount += entry.getValue().size();
				}
				System.out.println(vulCount);
				DependencyCache.putCache(connection, key, String.valueOf(vulCount));
        	}
        	
        	if(first == false)
        	{
        		leastVulCount = vulCount;
        		leastVulCountCombination = key;
        		first = true;
        	}
        	else
        	{
        		if(vulCount<leastVulCount)
        		{
        			leastVulCount = vulCount;
        			leastVulCountCombination = key;
        		}
        	}
        
	        leastVulCountCombinationMap.put(leastVulCountCombination, leastVulCount);
	        
		}catch (Exception e) {
			System.out.println(e);
		}
		return leastVulCountCombinationMap;
	}
}
