package pom;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.fasterxml.jackson.databind.ObjectMapper;

import common.Constants;
import common.dto.DependencyDTO;

public class DependencyOperations {

	//generate the dependency tree
	public static void generateDependencyTreeWithPath(String pomFileLocation, String pomLocation, String dependencyTreeLocation)
	{
		try {

			String[] dependencyTreeCommandWithPath = { Constants.mvnPath+"\\mvn.cmd", 
					"-f",
					pomFileLocation,
					"dependency:tree", 
					"-DoutputType=json", 
					"-DoutputFile="+dependencyTreeLocation };

			ProcessBuilder processBuilder = new ProcessBuilder(dependencyTreeCommandWithPath);
			processBuilder.directory(new java.io.File(pomLocation));
			Process process = processBuilder.start();

			// Read the output (optional, since output is redirected to a file)
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
			}

			// Wait for the process to complete
			int exitCode = process.waitFor();
//			System.out.println("Exited with code: " + exitCode);

			// Verify the output file
			java.io.File outputFile = new java.io.File(dependencyTreeLocation);
			if (outputFile.exists()) {
//				System.out.println("Dependency tree successfully written to " + outputFile.getAbsolutePath());
			} else {
//				System.out.println("Failed to create dependency tree file.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static DependencyDTO getDependencyTree(String dependencyTreeLocation)
	{
		//		System.out.println("inside getDependencyTree");
		ObjectMapper objectMapper = new ObjectMapper();
		DependencyDTO rootDependency = new DependencyDTO();
		try {
			// Read JSON file and convert it to a List of User objects
			rootDependency = objectMapper.readValue(new File(dependencyTreeLocation), DependencyDTO.class);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return rootDependency;
	}

	public static void generateDependencyCheckReportWithPath(String pomFileLocation, String pomLocation, String dependencyCheckReportLocation)
	{
		//		System.out.println("inside generateDependencyCheckReportWithPath");
		try {

			String[] dependencyCheckCommand = { Constants.mvnPath+"\\mvn.cmd", 
					"-f",
					pomFileLocation,
					"dependency-check:check" };

			// Start the process
			ProcessBuilder processBuilder = new ProcessBuilder(dependencyCheckCommand);
			processBuilder.directory(new java.io.File(pomLocation));
			Process process = processBuilder.start();

			// Read the output (optional, since output is redirected to a file)
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
			}

			// Wait for the process to complete
			int exitCode = process.waitFor();
			//			System.out.println("Exited with code: " + exitCode);

			// Verify the output file
			java.io.File outputFile = new java.io.File(dependencyCheckReportLocation);
			if (outputFile.exists()) {
				//				System.out.println("Dependency tree successfully written to " + outputFile.getAbsolutePath());
			} else {
				System.out.println("Failed to create dependency tree file.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
