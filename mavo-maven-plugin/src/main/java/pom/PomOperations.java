package pom;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import common.Constants;
import common.dto.POMDependencyDTO;

public class PomOperations {

	public static List<POMDependencyDTO> getDepndenciesFromPOM(String pomFilePath)
	{
		List<POMDependencyDTO> pomDependencies = new ArrayList<>();

		try
		{
			FileReader reader = new FileReader(pomFilePath);

			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(reader);

			List<Dependency> dependencies = model.getDependencies();

			
			for (Dependency dependency : dependencies){
				
				String version = dependency.getVersion();
				String propertyName = "";
				if(version != null)
				{
					if (version.startsWith("${") && version.endsWith("}")) {
	                    propertyName = version.substring(2, version.length() - 1);
	                    version = Constants.globalpropertiesMap.get(propertyName);
					}
					pomDependencies.add(new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), version));
				}
				else
				{
					for(POMDependencyDTO dep : Constants.depMgmtDependencies)
					{
						if(dependency.getGroupId().equals(dep.getGroupId()) && dependency.getArtifactId().equals(dep.getArtifactId()))
						{
							String depMgmtVersion = dep.getVersion();
							String depMgmtPropertyName = "";
							if (depMgmtVersion.startsWith("${") && depMgmtVersion.endsWith("}")) {
								depMgmtPropertyName = depMgmtVersion.substring(2, depMgmtVersion.length() - 1);
			                    depMgmtVersion = Constants.globalpropertiesMap.get(depMgmtPropertyName);
							}
							pomDependencies.add(new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), depMgmtVersion));
							break;
						}
					}
				}
			}
		}catch (Exception e) {
			System.out.println(e);
		}
		return pomDependencies;
	}
	
	public static List<Dependency> getDepMgmtDepndenciesFromPOM(String pomPath)
	{
		List<Dependency> dependencies = new ArrayList<>();
		try
		{
			FileReader reader = new FileReader(pomPath);

			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(reader);

			DependencyManagement depMgmt = model.getDependencyManagement();
			
			if(depMgmt != null)
			{
				dependencies = depMgmt.getDependencies();
			}

		}catch (Exception e) {
			System.out.println(e);
		}
		
		return dependencies;
	}

//	public static String getParentVersion(String pomPath)
//	{
//		String parentVersion = "";
//		try
//		{
//			FileReader reader = new FileReader(pomPath);
//
//			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
//			Model model = xpp3Reader.read(reader);
//
//			Parent parent = model.getParent();
////			System.out.println("Parent Version: " + parent.getVersion());
//			
//			parentVersion = parent.getVersion();
//		}catch (Exception e) {
//			System.out.println(e);
//		}
//		
//		return parentVersion;
//	}
//
//	public static List<List<POMDependencyDTO>> getParentDirectAndIndirectDepMgmtDependencies(String pomPath)
//	{
//		List<POMDependencyDTO> parentDependencies = new ArrayList<>();
//		List<POMDependencyDTO> directDependencies = new ArrayList<>();
//		List<POMDependencyDTO> externalDependencies = new ArrayList<>();
//		
//		try
//		{
//			FileReader reader = new FileReader(pomPath);
//
//			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
//			Model model = xpp3Reader.read(reader);
//
////			Parent parent = model.getParent();
////			System.out.println("Parent Version: " + parent);
//
//			DependencyManagement depMgmt = model.getDependencyManagement();
//			List<Dependency> dependencies = new ArrayList<>();
//			if(depMgmt != null)
//			{
//				dependencies = depMgmt.getDependencies();
////				System.out.println("dep size : "+dependencies.size());
//
//				for (Dependency dependency : dependencies){
//					POMDependencyDTO pomDependency = new POMDependencyDTO();
//					
//					if(dependency.getType().equalsIgnoreCase("pom"))
//					{
//						String version = dependency.getVersion();
//						if (version != null && version.startsWith("${") && version.endsWith("}")) {
//		                    String propertyName = version.substring(2, version.length() - 1);
//		                    version = Constants.globalpropertiesMap.get(propertyName);
////		                    Constants.globalpropertiesMap.remove(propertyName);
//							Constants.parentPropertiesMap.put(propertyName, version);
//						}
//						pomDependency = new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), version);
//						parentDependencies.add(pomDependency);
//					}
//					else
//					{
//						String version = dependency.getVersion();
//						String propertyName = "";
//						if (version != null && version.startsWith("${") && version.endsWith("}")) {
//		                    propertyName = version.substring(2, version.length() - 1);
//		                    version = Constants.globalpropertiesMap.get(propertyName);
//						}
//						pomDependency = new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), version);
//						
//						if(!Constants.excludeList.containsKey(pomDependency.getGroupId()) && !Constants.excludeList.containsValue(pomDependency.getArtifactId()))
//						{
//							if(Constants.parentPropertiesMap.containsKey(propertyName) && 
//									Constants.parentPropertiesMap.get(propertyName).equalsIgnoreCase(pomDependency.getVersion()))
//							{
//								directDependencies.add(pomDependency);
//							}
//							else
//							{
//								externalDependencies.add(pomDependency);
//							}							
//						}
//					}
//				}
//			}
//
//		}catch (Exception e) {
//			System.out.println(e);
//		}
//		
//		List<List<POMDependencyDTO>> dependencies = new ArrayList<>(); 
//		dependencies.add(parentDependencies);
//		dependencies.add(directDependencies);
//		dependencies.add(externalDependencies);
//		
//		return dependencies;
//	}
//	
//	public static List<List<POMDependencyDTO>> getParentDirectAndIndirectDependencies(String pomPath)
//	{
//		List<POMDependencyDTO> parentDependencies = new ArrayList<>();
//		List<POMDependencyDTO> directDependencies = new ArrayList<>();
//		List<POMDependencyDTO> externalDependencies = new ArrayList<>();
//		
//		try
//		{
//			FileReader reader = new FileReader(pomPath);
//
//			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
//			Model model = xpp3Reader.read(reader);
//
////			Parent parent = model.getParent();
////			System.out.println("Parent Version: " + parent);
//
//			List<Dependency> dependencies = model.getDependencies();
////			System.out.println("dep size : "+dependencies.size());
//
//			for (Dependency dependency : dependencies){
//				POMDependencyDTO pomDependency = new POMDependencyDTO();
//				
//				if(dependency.getType().equalsIgnoreCase("pom"))
//				{
//					String version = dependency.getVersion();
//					if (version != null && version.startsWith("${") && version.endsWith("}")) {
//	                    String propertyName = version.substring(2, version.length() - 1);
//	                    version = Constants.globalpropertiesMap.get(propertyName);
//	                    Constants.globalpropertiesMap.remove(propertyName);
//						Constants.parentPropertiesMap.put(propertyName, version);
//					}
//					pomDependency = new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), version);
//					parentDependencies.add(pomDependency);
//				}
//				else
//				{
//					String version = dependency.getVersion();
//					String propertyName = "";
//					if (version != null && version.startsWith("${") && version.endsWith("}")) {
//	                    propertyName = version.substring(2, version.length() - 1);
//	                    version = Constants.globalpropertiesMap.get(propertyName);
//					}
//					pomDependency = new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), version);
//					
//					if(!Constants.excludeList.containsKey(pomDependency.getGroupId()) && !Constants.excludeList.containsValue(pomDependency.getArtifactId()))
//					{
//						if(Constants.parentPropertiesMap.containsKey(propertyName) && 
//								Constants.parentPropertiesMap.get(propertyName).equalsIgnoreCase(pomDependency.getVersion()))
//						{
//							directDependencies.add(pomDependency);
//						}
//						else
//						{
//							externalDependencies.add(pomDependency);
//						}
//					}
//				}
//			}
//		}catch (Exception e) {
//			System.out.println(e);
//		}
//		
//		List<List<POMDependencyDTO>> dependencies = new ArrayList<>(); 
//		dependencies.add(parentDependencies);
//		dependencies.add(directDependencies);
//		dependencies.add(externalDependencies);
//		
//		return dependencies;
//	}

	public static List<List<POMDependencyDTO>> getSegregatedDependencies(String pomPath, boolean includeDepMgmtDependencies)
	{
		System.out.println("inside getSegregatedDependencies");
		List<POMDependencyDTO> parentDependencies = new ArrayList<>();
		List<POMDependencyDTO> externalDependencies = new ArrayList<>();
		try
		{
			FileReader reader = new FileReader(pomPath);

			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(reader);

			List<Dependency> dependencies = new ArrayList<>();
			if(includeDepMgmtDependencies)
			{
				DependencyManagement depMgmt = model.getDependencyManagement();
				if(depMgmt != null)
				{
					dependencies.addAll(depMgmt.getDependencies());
					for(Dependency dep : depMgmt.getDependencies())
					{
						Constants.depMgmtDependencies.add(new POMDependencyDTO(dep.getGroupId(), dep.getArtifactId(), dep.getVersion()));
					}
				}
			}
			dependencies.addAll(model.getDependencies());

			for (Dependency dependency : dependencies){
				POMDependencyDTO pomDependency = new POMDependencyDTO();
				
				if(dependency.getType().equalsIgnoreCase("pom"))
				{
					String version = dependency.getVersion();
					if (version != null && version.startsWith("${") && version.endsWith("}")) {
	                    String propertyName = version.substring(2, version.length() - 1);
	                    version = Constants.globalpropertiesMap.get(propertyName);
					}
					pomDependency = new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), version);
					parentDependencies.add(pomDependency);
				}
				else
				{
					String version = dependency.getVersion();
					String propertyName = "";
					if(version != null)
					{
						if (version.startsWith("${") && version.endsWith("}")) {
		                    propertyName = version.substring(2, version.length() - 1);
		                    version = Constants.globalpropertiesMap.get(propertyName);
						}
	                    pomDependency = new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), version);
						
						if(!Constants.excludeList.containsKey(pomDependency.getGroupId()) && !Constants.excludeList.containsValue(pomDependency.getArtifactId()))
						{
							externalDependencies.add(pomDependency);
						}
					}
					else
					{
						for(POMDependencyDTO dep : Constants.depMgmtDependencies)
						{
							if(dependency.getGroupId().equals(dep.getGroupId()) && dependency.getArtifactId().equals(dep.getArtifactId()))
							{
								String depMgmtVersion = dep.getVersion();
								String depMgmtPropertyName = "";
								if (depMgmtVersion.startsWith("${") && depMgmtVersion.endsWith("}")) {
									depMgmtPropertyName = depMgmtVersion.substring(2, depMgmtVersion.length() - 1);
				                    depMgmtVersion = Constants.globalpropertiesMap.get(depMgmtPropertyName);
								}
			                    pomDependency = new POMDependencyDTO(dependency.getGroupId(), dependency.getArtifactId(), depMgmtVersion);
								if(!Constants.excludeList.containsKey(pomDependency.getGroupId()) && !Constants.excludeList.containsValue(pomDependency.getArtifactId()))
								{
									externalDependencies.add(pomDependency);
								}
								break;
							}
						}
					}
				}
			}
			
		}catch (Exception e) {
			System.out.println(e);
		}
		
		List<List<POMDependencyDTO>> dependencies = new ArrayList<>(); 
		dependencies.add(parentDependencies);
		dependencies.add(externalDependencies);
		
		return dependencies;
	}

	public static void addToDependencyMap(List<POMDependencyDTO> dependencies, String moduleName)
	{
		for(POMDependencyDTO dependency : dependencies)
		{
			String mergedGroupIdVersion = dependency.getGroupId()+"_"+dependency.getVersion();
			
			if(Constants.dependencyMap.containsKey(mergedGroupIdVersion))
			{
				List<String> dependencyList = Constants.dependencyMap.get(mergedGroupIdVersion);
				dependencyList.add(dependency.getArtifactId()+"_"+moduleName);
				Constants.dependencyMap.put(mergedGroupIdVersion, dependencyList);
			}
			else
			{
				List<String> dependencyList = new ArrayList<>();
				dependencyList.add(dependency.getArtifactId()+"_"+moduleName);
				Constants.dependencyMap.put(mergedGroupIdVersion, dependencyList);
			}
		}
	}
	
	public static void clearAllDependencies(String pomFilePath){
		try
		{
			FileReader reader = new FileReader(pomFilePath);

			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(reader);
			
			DependencyManagement depMgmt = model.getDependencyManagement();
			if(depMgmt!=null)
			{
				depMgmt.getDependencies().clear();
			}
			model.getDependencies().clear();			
			
            try (FileWriter writer = new FileWriter(pomFilePath)) {
                MavenXpp3Writer mavenWriter = new MavenXpp3Writer();
                mavenWriter.write(writer, model);
            }
		}catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void addDependency(String pomFilePath, String groupId, String artifactId, String version){

		try {
			File pomFile = new File(pomFilePath);

			// Parse the existing pom.xml
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(pomFile);
			doc.getDocumentElement().normalize();

			// Check if the dependency already exists with the same groupId and artifactId
			NodeList dependenciesList = doc.getElementsByTagName("dependency");
			boolean dependencyExists = false;

			for (int i = 0; i < dependenciesList.getLength(); i++) {
				Element dependency = (Element) dependenciesList.item(i);

				String currentGroupId = dependency.getElementsByTagName("groupId").item(0).getTextContent();
				String currentArtifactId = dependency.getElementsByTagName("artifactId").item(0).getTextContent();

				// If the groupId and artifactId match, check version
				if (currentGroupId.equals(groupId) && currentArtifactId.equals(artifactId)) {
					String currentVersion = dependency.getElementsByTagName("version").item(0).getTextContent();

					if (!currentVersion.equals(version)) {
						// If the version is different, update the version
						dependency.getElementsByTagName("version").item(0).setTextContent(version);
//						System.out.println("Updated dependency version from " + currentVersion + " to " + version);
					} else {
//						System.out.println("Dependency already exists with the same version.");
					}

					// Dependency with same groupId and artifactId found, no need to add a duplicate
					dependencyExists = true;
					break;
				}
			}

			// If no matching dependency is found, add a new one
			if (!dependencyExists) {
				// Find or create the <dependencies> element
				NodeList dependenciesListRoot = doc.getElementsByTagName("dependencies");
				Element dependencies;
				if (dependenciesListRoot.getLength() > 0) {
					dependencies = (Element) dependenciesListRoot.item(0);
				} else {
					// Create <dependencies> block if it doesn't exist
					dependencies = doc.createElement("dependencies");
					doc.getDocumentElement().appendChild(dependencies);
				}

				// Create the new dependency element
				Element newDependency = doc.createElement("dependency");

				Element groupIdElem = doc.createElement("groupId");
				groupIdElem.appendChild(doc.createTextNode(groupId));
				newDependency.appendChild(groupIdElem);

				Element artifactIdElem = doc.createElement("artifactId");
				artifactIdElem.appendChild(doc.createTextNode(artifactId));
				newDependency.appendChild(artifactIdElem);

				Element versionElem = doc.createElement("version");
				versionElem.appendChild(doc.createTextNode(version));
				newDependency.appendChild(versionElem);

				// Add the new dependency
				dependencies.appendChild(newDependency);
//				System.out.println("Dependency added successfully to pom.xml.");
			}

			TempPomCreator.removeWhitespaceNodes(doc.getDocumentElement());

			// Save the updated document back to the pom.xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(pomFile);
			transformer.transform(source, result);
		}catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void updateDependency(String pomFileLocation, String groupId, String artifactId, String version) {
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(pomFileLocation));

			// Normalize the XML structure
			doc.getDocumentElement().normalize();

			Node dependency = doc.getElementsByTagName("dependency").item(0);
			if (dependency.getNodeType() == Node.ELEMENT_NODE) {
				Element dependencyElement = (Element) dependency;

				Node groupIdNode = dependencyElement.getElementsByTagName("groupId").item(0);
				Node artifactIdNode = dependencyElement.getElementsByTagName("artifactId").item(0);
				Node versionNode = dependencyElement.getElementsByTagName("version").item(0);

				groupIdNode.setTextContent(groupId);
				artifactIdNode.setTextContent(artifactId);
				versionNode.setTextContent(version);
			}

			// Write the updated document back to the file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(pomFileLocation));
			//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
//			System.out.println("POM file updated successfully to - "+groupId+" - "+artifactId+" - "+version);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateDependencyVersion(String pomFileLocation, String groupId, String artifactId, String newVersion) {
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(pomFileLocation));

			// Normalize the XML structure
			doc.getDocumentElement().normalize();

			// Get the list of dependencies
			NodeList dependencies = doc.getElementsByTagName("dependency");

			// Iterate through the dependencies to find the correct one
			for (int i = 0; i < dependencies.getLength(); i++) {
				Node dependency = dependencies.item(i);

				if (dependency.getNodeType() == Node.ELEMENT_NODE) {
					Element dependencyElement = (Element) dependency;

					String currentGroupId = dependencyElement.getElementsByTagName("groupId").item(0).getTextContent();
					String currentArtifactId = dependencyElement.getElementsByTagName("artifactId").item(0).getTextContent();

					if (currentGroupId.equals(groupId) && currentArtifactId.equals(artifactId)) {
						// Found the dependency, now update the version
						Node versionNode = dependencyElement.getElementsByTagName("version").item(0);
						if (versionNode != null) {
							versionNode.setTextContent(newVersion);
//                            System.out.println("Updated version to " + newVersion);
						} else {
							// If no version tag exists, add it
							Element versionElement = doc.createElement("version");
							versionElement.appendChild(doc.createTextNode(newVersion));
							dependencyElement.appendChild(versionElement);
//                            System.out.println("Added version " + newVersion);
						}
						break; // Dependency found and updated; exit loop
					}
				}
			}

			// Write the updated document back to the file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(pomFileLocation));
			//            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);

//			System.out.println("POM file updated successfully to version - "+newVersion);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> getPomModules(String pomFilePath)
	{
		List<String> modules = new ArrayList<>();
		try
		{
			FileReader reader = new FileReader(pomFilePath);

			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(reader);

			modules =  model.getModules();

		}catch (Exception e) {
			System.out.println(e);
		}
		
		return modules;
	}
	
	public static Map<String, String> getPomProperties(String pomPath)
	{
//		Properties modules = new ArrayList<>();
		Map<String, String> propertiesMap = new HashMap<>();
		try
		{
			FileReader reader = new FileReader(pomPath);

			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(reader);

			Properties properties = model.getProperties();
			Set<Object> keys = properties.keySet();
			for(Object key : keys)
			{
				propertiesMap.put(key.toString(), properties.getProperty(key.toString()));
			}

		}catch (Exception e) {
			System.out.println(e);
		}
		
		return propertiesMap;
	}

	public static void updatePomProperties(String pomPath, String groupId, String artifactId, String version)
	{
		System.out.println("inside updatePomProperties");
		try
		{
			FileReader reader = new FileReader(pomPath);

			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			Model model = xpp3Reader.read(reader);

			List<Dependency> dependencies = new ArrayList<>();
			DependencyManagement depMgmt = model.getDependencyManagement();
			if(depMgmt!=null)
			{
				dependencies.addAll(depMgmt.getDependencies());				
			}
			
			dependencies.addAll(model.getDependencies());
			
			for(Dependency dependency : dependencies)
			{
				if(dependency.getGroupId().equalsIgnoreCase(groupId) && dependency.getArtifactId().equalsIgnoreCase(artifactId))
				{
					String depVersion = dependency.getVersion();
					if(depVersion != null)
					{
						if(depVersion.startsWith("${") && depVersion.endsWith("}"))
						{
			                String propertyName = depVersion.substring(2, depVersion.length() - 1);
			                if(Constants.globalpropertiesMap.containsKey(propertyName))
			                {
			                    Constants.globalpropertiesMap.replace(propertyName, version);
			                }
//			                if(Constants.parentPropertiesMap.containsKey(propertyName))
//			                {
//			                    Constants.parentPropertiesMap.replace(propertyName, version);
//			                }
						}
					}
				}
			}

		}catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	
	public static void updateParentDependencies(String groupId, String artifactId, String version)
	{
		for(POMDependencyDTO p : Constants.parentDependencies)
		{
			if(p.getGroupId().equals(groupId) && p.getArtifactId().equals(artifactId))
			{
				p.setVersion(version);
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	public static void addToDependencyMap(List<POMDependencyDTO> dependencies)
//	{
//		for(POMDependencyDTO dependency : dependencies)
//		{
////			String mergedGroupIdVersion = dependency.getGroupId()+"_"+dependency.getVersion();
//			String mergedGroupIdVersion = dependency.getGroupId();
//			
//			if(Constants.dependencyMap.containsKey(mergedGroupIdVersion))
//			{
//				List<POMDependencyDTO> dependencyList = Constants.dependencyMap.get(mergedGroupIdVersion);
//				dependencyList.add(dependency);
//				Constants.dependencyMap.put(mergedGroupIdVersion, dependencyList);
//			}
//			else
//			{
//				List<POMDependencyDTO> dependencyList = new ArrayList<>();
//				dependencyList.add(dependency);
//				Constants.dependencyMap.put(mergedGroupIdVersion, dependencyList);
//			}
//		}
//	}
	
	
//	public static void clearAllDependencies(String pomFilePath){
//	
//	try
//	{
//		File pomFile = new File(pomFilePath);
//
//		// Parse the existing pom.xml
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder builder = factory.newDocumentBuilder();
//		Document doc = builder.parse(pomFile);
//		doc.getDocumentElement().normalize();
//
//		// Get the <dependencies> block
//		NodeList dependenciesList = doc.getElementsByTagName("dependencies");
//
//		if (dependenciesList.getLength() > 0) {
//			System.out.println(dependenciesList.getLength());
//			Node dependenciesNode = dependenciesList.item(0);
//
//			// Remove all <dependency> elements under <dependencies>
//			NodeList dependencyElements = dependenciesNode.getChildNodes();
//			while (dependencyElements.getLength() > 0) {
//				dependenciesNode.removeChild(dependencyElements.item(0));
//			}
//
////			System.out.println("All dependencies have been cleared.");
//		} else {
////			System.out.println("No <dependencies> block found.");
//		}
//
//		// Save the updated document back to the pom.xml file
//		TransformerFactory transformerFactory = TransformerFactory.newInstance();
//		Transformer transformer = transformerFactory.newTransformer();
//		DOMSource source = new DOMSource(doc);
//		StreamResult result = new StreamResult(pomFile);
//		transformer.transform(source, result);
//	}catch (Exception e) {
//		System.out.println(e);
//	}
//}
}
