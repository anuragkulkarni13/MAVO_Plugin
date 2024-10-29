package reporting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

import common.Constants;
import common.dto.POMDependencyDTO;
import common.dto.VulnerabilityDTO;
import pom.DependencyOperations;
import pom.PomOperations;
import vulnerability.VulnerabilityAnalyzer;

public class Reporting {

	public static void createReport(String originalPomPath, String pomPath, Map<String, List<POMDependencyDTO>> finalChanges)
	{
		System.out.println("Module - "+pomPath);
		
		String[] arr = pomPath.split("\\\\");
		String pomModule = arr[arr.length-1];
		
		List<POMDependencyDTO> depMgmtRecommendations = new ArrayList<>();
		List<POMDependencyDTO> depRecommendations = new ArrayList<>();
				
		if(Constants.finalDepMgmtChanges.containsKey(pomModule))
		{
			depMgmtRecommendations = Constants.finalDepMgmtChanges.get(pomModule);
		}
		
		if(finalChanges.containsKey(pomModule))
		{
			depRecommendations = finalChanges.get(pomModule);
		}
		
//		System.out.println();
//		System.out.println(pomModule);
//		System.out.println("################# depMgmtRecommendations");
//		for(POMDependencyDTO dependency : depMgmtRecommendations)
//	    {
//	    	System.out.println(dependency.getGroupId());
//	    	System.out.println(dependency.getArtifactId());
//	    	System.out.println(dependency.getVersion());
//	    }
//		
//		System.out.println("################# depRecommendations");
//		for(POMDependencyDTO dependency : depRecommendations)
//	    {
//	    	System.out.println(dependency.getGroupId());
//	    	System.out.println(dependency.getArtifactId());
//	    	System.out.println(dependency.getVersion());
//	    }		
		
		List<Dependency> OriginalDepMgmtList = PomOperations.getDepMgmtDepndenciesFromPOM(pomPath+Constants.pomFileName);
		List<POMDependencyDTO> OriginalDepList = PomOperations.getDepndenciesFromPOM(pomPath+Constants.pomFileName);
		
		createModuleReport(originalPomPath, pomModule, OriginalDepMgmtList, OriginalDepList, depMgmtRecommendations, depRecommendations);
		
		List<String> modules = PomOperations.getPomModules(pomPath+Constants.pomFileName);
		
		for(String module : modules)
		{
//			System.out.println(module);
			createReport(originalPomPath, pomPath+"\\"+module, finalChanges);
		}
	}
	
	public static void createModuleReport(String originalPomPath, String moduleName, List<Dependency> OriginalDepMgmtList, List<POMDependencyDTO> OriginalDepList, List<POMDependencyDTO> depMgmtRecommendations, List<POMDependencyDTO> depRecommendations)
	{
		String newPomFilePath = originalPomPath+Constants.tempPomFileDirectoryName+Constants.tempPomFileName;
		String newPomPath = originalPomPath+Constants.tempPomFileDirectoryName;
		String newPomDependencyCheckRepotPath = newPomPath+Constants.dependencyCheckReportName;
        
		try {
            // Create a PdfWriter instance
	        Path directoryPath = Paths.get(originalPomPath+Constants.reportsDirectoryName);

	        // Check if the directory exists
	        if (!Files.exists(directoryPath) || !Files.isDirectory(directoryPath)) {
	            System.out.println("Directory does not exist: " + originalPomPath+Constants.reportsDirectoryName);
				Files.createDirectories(Paths.get(originalPomPath+Constants.reportsDirectoryName));
	        } else {
	            System.out.println("Directory exists: " + originalPomPath+Constants.reportsDirectoryName);
	        }
	        
            LocalDateTime currentDateTime = LocalDateTime.now();

            // Define the format for the date and time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Format the current date and time
            String formattedDateTime = currentDateTime.format(formatter);
            
            PdfWriter writer = new PdfWriter(originalPomPath+Constants.reportsDirectoryName+Constants.reportsFileName+"_"+moduleName+"_"+System.currentTimeMillis()+".pdf");

            // Create a PdfDocument instance
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Create a Document instance
            Document document = new Document(pdfDoc);

            Text Heading = new Text("MAVO Report").setFontSize(20); // 20pt font
            
            document.add(new Paragraph().add(Heading));
            document.add(new Paragraph().add("Date of report generation - "+formattedDateTime));
//            document.add(new Paragraph().add("Date Limit for Vulnerability analysis - "+zdt+"\n\n"));
            
            document.add(new Paragraph(new Text("Original POM").setFontSize(16)));
			PomOperations.clearAllDependencies(newPomFilePath);

			document.add(new Paragraph("<dependencyManagement>"));
            for(Dependency dep : OriginalDepMgmtList)
			{
	            document.add(new Paragraph(".        <dependency>"));
	            document.add(new Paragraph(".                <groupId>"+dep.getGroupId()+"<groupId>"));
	            document.add(new Paragraph(".                <artifactId>"+dep.getArtifactId()+"<artifactId>"));
	            document.add(new Paragraph(".                <version>"+dep.getVersion()+"<version>"));
	            document.add(new Paragraph(".        </dependency>"));
			}
            document.add(new Paragraph("</dependencyManagement>"));
            for(POMDependencyDTO dep : OriginalDepList)
			{
	            PomOperations.addDependency(newPomFilePath, dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
	            document.add(new Paragraph("<dependency>"));
	            document.add(new Paragraph(".        <groupId>"+dep.getGroupId()+"<groupId>"));
	            document.add(new Paragraph(".        <artifactId>"+dep.getArtifactId()+"<artifactId>"));
	            document.add(new Paragraph(".        <version>"+dep.getVersion()+"<version>"));
	            document.add(new Paragraph("</dependency>"));
			}
			int vulCount = 0;
	        DependencyOperations.generateDependencyCheckReportWithPath(newPomFilePath, newPomPath, newPomDependencyCheckRepotPath);
			Map<String, List<VulnerabilityDTO>> vulnerabilityMap = VulnerabilityAnalyzer.getVulnerabilityList(newPomDependencyCheckRepotPath);
			
			for (Map.Entry<String, List<VulnerabilityDTO>> entry : vulnerabilityMap.entrySet()) {
	//		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().size());
			    vulCount += entry.getValue().size();
			}
			System.out.println("Old POM - "+vulCount);
			document.add(new Paragraph(new Text("Original POM Vulnerability count - "+vulCount).setFontSize(16)));
			
            document.add(new AreaBreak());
            document.add(new Paragraph(new Text("Recommended POM").setFontSize(16)));
			PomOperations.clearAllDependencies(newPomFilePath);

			document.add(new Paragraph("<dependencyManagement>"));
            for(POMDependencyDTO dep : depMgmtRecommendations)
			{
	            document.add(new Paragraph(".        <dependency>"));
	            document.add(new Paragraph(".                <groupId>"+dep.getGroupId()+"<groupId>"));
	            document.add(new Paragraph(".                <artifactId>"+dep.getArtifactId()+"<artifactId>"));
	            document.add(new Paragraph(".                <version>"+dep.getVersion()+"<version>"));
	            document.add(new Paragraph(".        </dependency>"));
			}
            document.add(new Paragraph("</dependencyManagement>"));
			for(POMDependencyDTO dep : depRecommendations)
			{
	            PomOperations.addDependency(newPomFilePath, dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
	            document.add(new Paragraph("<dependency>"));
	            document.add(new Paragraph(".        <groupId>"+dep.getGroupId()+"<groupId>"));
	            document.add(new Paragraph(".        <artifactId>"+dep.getArtifactId()+"<artifactId>"));
	            document.add(new Paragraph(".        <version>"+dep.getVersion()+"<version>"));
	            document.add(new Paragraph("</dependency>"));
			}
			int vulCount1 = 0;
	        DependencyOperations.generateDependencyCheckReportWithPath(newPomFilePath, newPomPath, newPomDependencyCheckRepotPath);
			Map<String, List<VulnerabilityDTO>> vulnerabilityMap1 = VulnerabilityAnalyzer.getVulnerabilityList(newPomDependencyCheckRepotPath);
			
			for (Map.Entry<String, List<VulnerabilityDTO>> entry : vulnerabilityMap1.entrySet()) {
	//		    System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().size());
			    vulCount1 += entry.getValue().size();
			}
			
			System.out.println("New POM - "+vulCount1);
			document.add(new Paragraph(new Text("Recommended POM Vulnerability count - "+vulCount1).setFontSize(16)));

            // Close the document
            document.close();

            System.out.println("PDF created successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
}
