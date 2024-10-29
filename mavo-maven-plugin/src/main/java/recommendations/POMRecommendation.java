package recommendations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cache.DependencyCache;
import common.Constants;
import common.dto.DependencyDTO;
import common.dto.POMDependencyDTO;
import pom.DependencyOperations;
import pom.PomOperations;
import vulnerability.VulnerabilityAnalyzer;

public class POMRecommendation {

	public static List<DependencyDTO> getRecommendationsForPOMDependency(POMDependencyDTO pomDependency, String pomPath)
	{
		String newPomPath = pomPath+Constants.tempPomFileDirectoryName;
		String newPomDependencyTreePath = newPomPath+Constants.dependencyTreeName;
		String newPomDependencyCheckRepotPath = newPomPath+Constants.dependencyCheckReportName;
		String newPomFilePath = pomPath+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		
//		System.out.println("inside getRecommendationsForPOMDependency");
		
		List<DependencyDTO> pomRecommendations = new ArrayList<DependencyDTO>();

		try {

			String DB_URL_VUL = DependencyCache.get_DB_URL_VUL(pomPath);

			Connection connection = DriverManager.getConnection(DB_URL_VUL);

			PomOperations.clearAllDependencies(newPomFilePath);

			PomOperations.addDependency(newPomFilePath, pomDependency.getGroupId(), pomDependency.getArtifactId(), pomDependency.getVersion());

			DependencyOperations.generateDependencyTreeWithPath(newPomFilePath, newPomPath, newPomDependencyTreePath);
			
			DependencyDTO root = DependencyOperations.getDependencyTree(newPomDependencyTreePath);
			
			for(DependencyDTO child : root.getChildren())
			{
				List<String> depList = new ArrayList<>();

				Queue<DependencyDTO> depQueue = new LinkedList<>();
				depList.add(child.getArtifactId());
				depQueue.offer(child);
//				System.out.println("child artifact id - "+child.getArtifactId());
				while(!depQueue.isEmpty())
				{
					DependencyDTO node = depQueue.poll();
//					System.out.println("parent "+node.getArtifactId());
					PomOperations.updateDependency(newPomFilePath, node.getGroupId(), node.getArtifactId(), node.getVersion());
					
					String leastVulCountVersion = node.getVersion();
					
					boolean includedInParent = false;
					for(POMDependencyDTO dep : Constants.parentDependencies)
					{
						if(dep.getGroupId().equals(node.getGroupId()) && dep.getVersion().equals(node.getVersion()))
						{
							includedInParent = true;
						}
					}
					
					if(!includedInParent)
//					if(!Constants.parentPropertiesMap.containsValue(leastVulCountVersion))
//					if(!leastVulCountVersion.equalsIgnoreCase(parentVersion))
					{
						leastVulCountVersion = VulnerabilityAnalyzer.getleastVulCountVersion(newPomFilePath, newPomPath, newPomDependencyTreePath, newPomDependencyCheckRepotPath, node.getGroupId(), node.getArtifactId(), node.getVersion(), connection);
						if(!leastVulCountVersion.equals(node.getVersion()))
						{
							System.out.println("############################# add dep ############################## "+node.getArtifactId()+" - "+node.getGroupId()+" - "+leastVulCountVersion);
							node.setVersion(leastVulCountVersion);
							pomRecommendations.add(node);
						}
					}
					
					PomOperations.updateDependencyVersion(newPomFilePath, node.getGroupId(),node.getArtifactId(),leastVulCountVersion);
					DependencyOperations.generateDependencyTreeWithPath(newPomFilePath, newPomPath, newPomDependencyTreePath);

					node = DependencyOperations.getDependencyTree(newPomDependencyTreePath);
					for(DependencyDTO nodeChild : node.getChildren().get(0).getChildren())
					{
						if(!depList.contains(nodeChild.getArtifactId()))
						{
							depList.add(nodeChild.getArtifactId());
							depQueue.offer(nodeChild);
//							System.out.println("child - "+nodeChild.getArtifactId());
						}
					}
				}
			}
			connection.close();

		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return pomRecommendations;
	}
}
