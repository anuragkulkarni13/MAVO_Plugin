package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;

import cache.DependencyCache;
import common.Constants;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import common.dto.VersionDTO;
import pom.PomOperations;
import pom.TempPomCreator;
import recommendations.POMOptimization;
import recommendations.POMRecommendation;
import reporting.Reporting;
import versionmanagement.VersionFetcher;

public class POMAnalyzerAlgorithm {

	public static void pomAnalyzerAlgorithm(String originalPomPath, boolean excludeParentDependencies, 
			boolean groupRecommendations, boolean optimizeRecommendations, String patchRestriction)
	{
		
		System.out.println("############################################ pomAnalyzerAlgorithm start ##########################################################");
		DependencyCache.createCache(originalPomPath);
		
		parseModule(originalPomPath, patchRestriction);
		
		vulnerabilityCheckModule(originalPomPath, originalPomPath, excludeParentDependencies, optimizeRecommendations);

		System.out.println("########################## dependencyMap ########################");
		for (Map.Entry<String, List<String>> entry : Constants.dependencyMap.entrySet()) {
		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}
		
		System.out.println("########################## keyModule ########################");
		for (Map.Entry<String, String> entry : Constants.keyModule.entrySet()) {
		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}
		
		updateDependencyMap(Constants.dependencyMap, Constants.keyModule);
		
		getDepMgmtRecommendations(originalPomPath);
		
		Map<String, List<POMDependencyDTO>> finalChanges = new HashMap<>();
		if(groupRecommendations)
		{
			finalChanges = getGroupRecommendations(Constants.dependencyMap, Constants.keyModule);			
		}
		else
		{
			finalChanges = getRecommendations(Constants.dependencyMap, Constants.keyModule);
		}

		Reporting.createReport(originalPomPath, originalPomPath, finalChanges);
		
		System.out.println("############################################ pomAnalyzerAlgorithm end ##########################################################");
	}
	
	public static void parseModule(String pomPath, String patchRestriction)
	{
		System.out.println("Module - "+pomPath);
		
		// temp pom creation and setup
		TempPomCreator.createAndSetupTempPOM(pomPath);
		
		// get properties
		String newPomFilePath = pomPath+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		
		Map<String, String> propertiesMap = PomOperations.getPomProperties(newPomFilePath);
		Constants.globalpropertiesMap.putAll(propertiesMap);

		List<List<POMDependencyDTO>> dependencies = PomOperations.getSegregatedDependencies(newPomFilePath, true);
		
		List<POMDependencyDTO> parentDeps = dependencies.get(0);
		
		Constants.parentDependencies.addAll(parentDeps);
		List<POMDependencyDTO> externalDeps = dependencies.get(1);
		
		System.out.println();
		System.out.println("parent dependencies");
		for(POMDependencyDTO p : parentDeps)
		{
			System.out.println(p.getGroupId());
			System.out.println(p.getArtifactId());
			System.out.println(p.getVersion());
		}
		
		System.out.println();
		System.out.println("external dependencies");
		for(POMDependencyDTO p : externalDeps)
		{
			System.out.println(p.getGroupId());
			System.out.println(p.getArtifactId());
			System.out.println(p.getVersion());
		}
		
		upgradeParentToLatestMajorPatch(parentDeps, newPomFilePath, patchRestriction);
		
		String[] arr = pomPath.split("\\\\");
		String moduleName = arr[arr.length-1];
		PomOperations.addToDependencyMap(externalDeps, moduleName);
		
		List<String> modules = PomOperations.getPomModules(pomPath+Constants.pomFileName);
		
		for(String module : modules)
		{
//			System.out.println(module);
			parseModule(pomPath+"\\"+module, patchRestriction);
		}
	}
	
	public static void vulnerabilityCheckModule(String originalPomPath, String pomPath, boolean excludeParentDependencies, boolean optimizeRecommendations)
	{
		System.out.println("Module - "+pomPath);
		
		// get properties
		String newPomFilePath = pomPath+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		
		List<List<POMDependencyDTO>> dependencies = PomOperations.getSegregatedDependencies(newPomFilePath, false);
		
		List<POMDependencyDTO> externalDependencies = dependencies.get(1);
		System.out.println("ext dep count : "+externalDependencies.size());
		
		List<POMDependencyDTO> allDependencies = new ArrayList<>();

		System.out.println("excludeParentDependencies : "+excludeParentDependencies);
		if(excludeParentDependencies)
		{
			System.out.println("inside excludeParentDependencies");
			for(POMDependencyDTO dependency : externalDependencies)
			{
				System.out.println("dependency - "+dependency.getArtifactId()+" - "+dependency.getVersion());
				boolean depInParent = false;
				for(POMDependencyDTO parDep : Constants.parentDependencies)
				{
					System.out.println("parent - "+parDep.getArtifactId()+" - "+parDep.getVersion());
					if(parDep.getGroupId().equals(dependency.getGroupId()) && parDep.getVersion().equals(dependency.getVersion()))
					{
						depInParent = true;
						break;
					}
				}
				if(!depInParent)
				{
					allDependencies.add(dependency);
				}
			}
		}
		else
		{
			allDependencies.addAll(externalDependencies);			
		}
		
		System.out.println("all dependencies size - "+allDependencies.size());
		
		
		List<DependencyDTO> pomRecommendations = new ArrayList<>();
		for(POMDependencyDTO pomDependency : allDependencies)
		{
			//get recommendations for the pomDependency
			List<DependencyDTO> pomDependencyRecommendations = POMRecommendation.getRecommendationsForPOMDependency(pomDependency, originalPomPath);
			for(DependencyDTO p : pomDependencyRecommendations)
			{
				boolean pomFind = false;
				for(DependencyDTO p1 : pomRecommendations)
				{
					if(p1.getGroupId().equalsIgnoreCase(p.getGroupId()) && p1.getArtifactId().equalsIgnoreCase(p.getArtifactId()) && p1.getVersion().equalsIgnoreCase(p.getVersion()))
					{
						pomFind = true;
						break;
					}
				}
				if(!pomFind)
				{
					pomRecommendations.add(p);
				}
			}
		}
		
		System.out.println();
		System.out.println("############################### depMgmt pom recommendations ##################################");
		for(DependencyDTO d : pomRecommendations)
		{
			System.out.println(d.getGroupId());
			System.out.println(d.getArtifactId());
			System.out.println(d.getVersion());
		}
		System.out.println("############################# depMgmt pom recommendations end ##################################");
		System.out.println();
		
		
		String tempPomFileDirectory = originalPomPath+Constants.tempPomFileDirectoryName;
		String tempPomFilePath = originalPomPath+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		String tempPomDependencyCheckReportLocation = originalPomPath+Constants.tempPomFileDirectoryName+Constants.dependencyCheckReportName;
		
		Map<String, Integer> leastVulCountCombinationMap = new HashMap<>();
		if(pomRecommendations.size()>0)
		{
			if(optimizeRecommendations)
			{
				leastVulCountCombinationMap = POMOptimization.optimizePOMRecommendations(originalPomPath, externalDependencies, pomRecommendations, tempPomFilePath, tempPomFileDirectory, tempPomDependencyCheckReportLocation);
			}
			else
			{
				leastVulCountCombinationMap = POMOptimization.organizePOMRecommendations(originalPomPath, externalDependencies, pomRecommendations, tempPomFilePath, tempPomFileDirectory, tempPomDependencyCheckReportLocation);				
			}
			System.out.println(leastVulCountCombinationMap);
		}
		
		
		for(String key : leastVulCountCombinationMap.keySet())
		{
			String[] arr = pomPath.split("\\\\");
			System.out.println(key+" : "+arr[arr.length-1]);
			Constants.keyModule.put(key, arr[arr.length-1]);
		}
		
		List<String> modules = PomOperations.getPomModules(pomPath+Constants.pomFileName);
		
		for(String module : modules)
		{
//			System.out.println(module);
			vulnerabilityCheckModule(originalPomPath, pomPath+"\\"+module, excludeParentDependencies, optimizeRecommendations);
		}
	}
	
	public static void updateDependencyMap(Map<String, List<String>> dependencyMap, Map<String, String> keyModule)
	{
		Map<String, String> changes = new HashMap<>();
		
		//update dependency Map
		for (Map.Entry<String, String> keymod : Constants.keyModule.entrySet()) {
		    System.out.println("Key: " + keymod.getKey() + ", Value: " + keymod.getValue());
		    String key = keymod.getKey();
		    String value = keymod.getValue();
		    String[] dependencies = key.split(",");
		    for(String dependency : dependencies)
		    {
		    	String[] arr = dependency.split("#");
		    	String keyGroupId = arr[0];
		    	String keyArtifactId = arr[1];
		    	String keyVersion = arr[2];
		    	
				for (Map.Entry<String, List<String>> depMap : Constants.dependencyMap.entrySet()) {
					String mergedGIDVersion = depMap.getKey();
					String[] brr = mergedGIDVersion.split("_");
					String depMapGroupID = brr[0];
					String depMapVersion = brr[1];
					
					if(keyGroupId.equals(depMapGroupID))
					{
						long depMapTimeStamp = VersionFetcher.getTimeStampforDependency(depMapGroupID, keyArtifactId, depMapVersion);
						long keyTimeStamp = VersionFetcher.getTimeStampforDependency(keyGroupId, keyArtifactId, keyVersion);
						
						if(depMapTimeStamp<keyTimeStamp)
						{
							String newMergedGIDVersion = depMapGroupID+"_"+keyVersion;
							changes.put(mergedGIDVersion, newMergedGIDVersion);
						}
					}
				}
		    }
		}
		
		for (Map.Entry<String, String> change : changes.entrySet())
		{
			String oldkey = change.getKey();
			String newkey = change.getValue();
			
	        if (dependencyMap.containsKey(oldkey)) {
	            // Get the value associated with the old key
	        	List<String> artifactIds = dependencyMap.get(oldkey);
	
	            // Remove the old key-value pair
	        	dependencyMap.remove(oldkey);
	
	            // Add the new key-value pair
	        	dependencyMap.put(newkey, artifactIds);
	        }
			
		}

		System.out.println("########################## updated dependencyMap ########################");
		for (Map.Entry<String, List<String>> entry : Constants.dependencyMap.entrySet()) {
		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
		}
	}
	
	public static void getDepMgmtRecommendations(String pomPath)
	{
		System.out.println("Module - "+pomPath);
		String[] pomPathArr = pomPath.split("\\\\");
		String pomModule = pomPathArr[pomPathArr.length-1];
		
		List<Dependency> dependencies = PomOperations.getDepMgmtDepndenciesFromPOM(pomPath+Constants.pomFileName);
		
		for(Dependency dependency : dependencies)
		{
			String depGroupId = dependency.getGroupId();
			String depArtifactId = dependency.getArtifactId();
			String depversion = dependency.getVersion();
			if (depversion != null && depversion.startsWith("${") && depversion.endsWith("}")) {
                String propertyName = depversion.substring(2, depversion.length() - 1);
                depversion = Constants.globalpropertiesMap.get(propertyName);
			}
			
			boolean depFound = false;
			for (Map.Entry<String, List<String>> depMap : Constants.dependencyMap.entrySet()) {
				String mergedGIDVersion = depMap.getKey();
				String[] brr = mergedGIDVersion.split("_");
				String depMapGroupID = brr[0];
				String depMapVersion = brr[1];
				
				if(depGroupId.equals(depMapGroupID))
				{
					depFound = true;
					POMDependencyDTO newDep = new POMDependencyDTO(depGroupId, depArtifactId, depMapVersion);
					List<POMDependencyDTO> depList = new ArrayList<>();
					if(Constants.finalDepMgmtChanges.containsKey(pomModule))
					{
						depList = Constants.finalDepMgmtChanges.get(pomModule);
					}
					depList.add(newDep);
					Constants.finalDepMgmtChanges.put(pomModule, depList);
					break;
				}
			}
			if(depFound == false)
			{
				POMDependencyDTO newDep = new POMDependencyDTO(depGroupId, depArtifactId, depversion);
				List<POMDependencyDTO> depList = new ArrayList<>();
				if(Constants.finalDepMgmtChanges.containsKey(pomModule))
				{
					depList = Constants.finalDepMgmtChanges.get(pomModule);
				}
				depList.add(newDep);
				Constants.finalDepMgmtChanges.put(pomModule, depList);
			}
		}
		
		List<String> modules = PomOperations.getPomModules(pomPath+Constants.pomFileName);
		
		for(String module : modules)
		{
//			System.out.println(module);
			getDepMgmtRecommendations(pomPath+"\\"+module);
		}
	}
	
	public static Map<String, List<POMDependencyDTO>> getRecommendations(Map<String, List<String>> dependencyMap, Map<String, String> keyModule)
	{
		Map<String, List<POMDependencyDTO>> finalChanges = new HashMap<>();
		for (Map.Entry<String, String> keymod : Constants.keyModule.entrySet()) {
		    System.out.println("Key: " + keymod.getKey() + ", Value: " + keymod.getValue());
		    
		    String key = keymod.getKey();
		    String module = keymod.getValue();
		    String[] dependencies = key.split(",");
		    for(String dependency : dependencies)
		    {
		    	String[] arr = dependency.split("#");
		    	String keyGroupId = arr[0];
		    	String keyArtifactId = arr[1];
		    	String keyVersion = arr[2];
		    	
				POMDependencyDTO newDep = new POMDependencyDTO(keyGroupId, keyArtifactId, keyVersion);
				List<POMDependencyDTO> depList = new ArrayList<>();
				if(finalChanges.containsKey(module))
				{
					depList = finalChanges.get(module);
				}
				depList.add(newDep);
				finalChanges.put(module, depList);
		    }
		}
		return finalChanges;
	}
	
	public static Map<String, List<POMDependencyDTO>> getGroupRecommendations(Map<String, List<String>> dependencyMap, Map<String, String> keyModule)
	{
		Map<String, List<POMDependencyDTO>> finalChanges = new HashMap<>();
		for (Map.Entry<String, String> keymod : Constants.keyModule.entrySet()) {
		    System.out.println("Key: " + keymod.getKey() + ", Value: " + keymod.getValue());
		    
		    String key = keymod.getKey();
		    String module = keymod.getValue();
		    String[] dependencies = key.split(",");
		    for(String dependency : dependencies)
		    {
		    	String[] arr = dependency.split("#");
		    	String keyGroupId = arr[0];
		    	String keyArtifactId = arr[1];
		    	String keyVersion = arr[2];
		    	
		    	boolean depFound = false;
				for (Map.Entry<String, List<String>> depMap : Constants.dependencyMap.entrySet()) {
					String mergedGIDVersion = depMap.getKey();
					String[] brr = mergedGIDVersion.split("_");
					String depMapGroupID = brr[0];
					String depMapVersion = brr[1];
					
					if(keyGroupId.equals(depMapGroupID))
					{
						depFound = true;
						POMDependencyDTO newDep = new POMDependencyDTO(keyGroupId, keyArtifactId, depMapVersion);
						List<POMDependencyDTO> depList = new ArrayList<>();
						if(finalChanges.containsKey(module))
						{
							depList = finalChanges.get(module);
						}
						depList.add(newDep);
						finalChanges.put(module, depList);
					}
				}
				if(depFound == false)
				{
					POMDependencyDTO newDep = new POMDependencyDTO(keyGroupId, keyArtifactId, keyVersion);
					List<POMDependencyDTO> depList = new ArrayList<>();
					if(finalChanges.containsKey(module))
					{
						depList = finalChanges.get(module);
					}
					depList.add(newDep);
					finalChanges.put(module, depList);
				}
		    }
		}
		return finalChanges;
	}
	
	public static void upgradeParentToLatestMajorPatch(List<POMDependencyDTO> parentDependencies, String newPomFilePath, String patchRestriction)
	{
		System.out.println("inside upgradeParentToLatestMajorPatch");
		System.out.println(patchRestriction);
		// loop over all the parent dependencies
		System.out.println();
		System.out.println("########## parent upgrade ##########");
		for(POMDependencyDTO depMgmtParentDependency : parentDependencies)
		{
			List<VersionDTO> versionList = VersionFetcher.fetchAllVersions(depMgmtParentDependency.getGroupId(), depMgmtParentDependency.getArtifactId(), depMgmtParentDependency.getVersion(), patchRestriction);
//			System.out.println(versionList);
			if(versionList.size()>0)
			{
				long maxTimestamp = 0L;
				String maxVersion = "";
				for(VersionDTO v : versionList)
				{
					if(v.getTimestamp()>maxTimestamp)
					{
						maxTimestamp = v.getTimestamp();
						maxVersion = v.getVersion();
					}
				}
				String updatedVerion = maxVersion;
				if(!updatedVerion.equalsIgnoreCase(depMgmtParentDependency.getVersion()))
				{
					//update the properties
					PomOperations.updatePomProperties(newPomFilePath, depMgmtParentDependency.getGroupId(), depMgmtParentDependency.getArtifactId(), updatedVerion);

					PomOperations.updateParentDependencies(depMgmtParentDependency.getGroupId(), depMgmtParentDependency.getArtifactId(), updatedVerion);
					
					System.out.println(depMgmtParentDependency.getGroupId());
					System.out.println(depMgmtParentDependency.getArtifactId());
					System.out.println(updatedVerion);

					System.out.println(Constants.globalpropertiesMap);
					System.out.println(Constants.parentDependencies);
				}
			}
		}
		System.out.println("########## parent upgrade ##########");
		System.out.println();
	}
}
