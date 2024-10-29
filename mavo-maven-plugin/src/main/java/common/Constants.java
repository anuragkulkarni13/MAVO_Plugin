package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.dto.POMDependencyDTO;


public class Constants {

	public static Map<String, String> globalpropertiesMap = new HashMap<>();
	
//	public static Map<String, String> parentPropertiesMap = new HashMap<>();

	public static Map<String, String> excludeList = new HashMap<>();
	
	public static List<POMDependencyDTO> parentDependencies = new ArrayList<>();

	public static List<POMDependencyDTO> depMgmtDependencies = new ArrayList<>();

	public static Map<String, List<String>> dependencyMap = new HashMap<>();
	
	public static Map<String, List<POMDependencyDTO>> finalDepMgmtChanges = new HashMap<>();

	public static Map<String, String> keyModule = new HashMap<>();

	public static String mvnPath = "C:\\Program Files\\apache-maven-3.9.8\\bin";
	
	public static int ROWS_PER_PAGE = 100;  // Number of results per page
	
//	public static String originalPomLocation = "D:\\Anurag\\MAVO Workspace\\parent";

	public static String pomFileName = "\\pom.xml";
	
	public static String tempPomFileDirectoryName = "\\tempPOMForDirectDependencies";
	
	public static String tempPomFileName = "\\temppom.xml";
	
	public static String dependencyTreeName = "\\dependency-tree.json";
	
	public static String dependencyCheckReportName = "\\dependency-check-report.json";
	
	public static String reportsDirectoryName = "\\Reports";
	
	public static String reportsFileName = "\\Vulnerability_Report";
	
	
//	public static String originalPomFileLocation = originalPomLocation+"\\pom.xml";
//
//	public static String newTempPomLocation = originalPomLocation+"\\tempPOMForDirectDependencies";
//
//	public static String newTempPomFileLocation = newTempPomLocation+"\\temppom.xml";
//	
//	public static String dependencyTreeLocation = newTempPomLocation+"\\dependency-tree.json";
//
//	public static String dependencyCheckReportLocation = newTempPomLocation + "\\dependency-check-report.json";
//
//	public static String filePath = newTempPomLocation + "\\VulMap.txt";
//	
//	public static String reportsDirectory = originalPomLocation+"\\Reports";
//	
//	public static String pdfFilePath = reportsDirectory+"\\Vulnerability_Report";
//
//	public static String DB_DIRECTORY_VUL = originalPomLocation;
	public static String DB_NAME_VUL = "VulnerabilityCache.db";
//	public static String DB_URL_VUL = "jdbc:sqlite:" + DB_DIRECTORY_VUL + "\\" + DB_NAME_VUL;
//	
//	public static String DB_DIRECTORY_COMB = originalPomLocation;
	public static String DB_NAME_COMB = "CombinationsCache.db";
//	public static String DB_URL_COMB = "jdbc:sqlite:" + DB_DIRECTORY_COMB + "\\" + DB_NAME_COMB;

	
	
	
	
//	public static Map<String, List<POMDependencyDTO>> dependencyMap = new HashMap<>();

//	public static boolean restrictToMajorPatchVersion = false;
	
//	public static long toTime = 1677177000000L;
	
//	public static String originalPomLocation = "C:\\Users\\Anurag Kulkarni\\OneDrive\\Desktop\\POMS";
	
}
