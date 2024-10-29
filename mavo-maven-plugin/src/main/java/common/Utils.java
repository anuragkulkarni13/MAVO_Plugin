package common;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.dto.DependencyDTO;

public class Utils {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	// Convert Map<String, Integer> to JSON String
	public static String mapToJson(Map<String, Integer> map) {
		try {
			return objectMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "{}";  // Return empty JSON on error
		}
	}

	// Convert JSON String to Map<String, Integer>
	public static Map<String, Integer> jsonToMap(String json) {
		try {
			return objectMapper.readValue(json, Map.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return new HashMap<>();  // Return empty Map on error
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return new HashMap<>();  // Return empty Map on error
		}
	}

	public static void writeMapToFile(String filePath, DependencyDTO root, Map<String,Integer> vulCount)
	{
		//		String pomLocation = "D:\\Anurag\\pom\\temppom";
		//
		//		String filePath = pomLocation + "\\VulMap.txt";

		// Write the HashMap to the file
		try (FileWriter writer = new FileWriter(filePath, true)) {
			writer.write("#######################################################\n");
			writer.write(root.getGroupId() +" - "+ root.getArtifactId() +" - "+ root.getVersion() +"\n");
			for (Map.Entry<String, Integer> entry : vulCount.entrySet()) {
				writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
			}
			int totalValue = 0;
			for (Integer value : vulCount.values()) {
				totalValue += value;
			}
			writer.write("total value - "+totalValue+"\n");
			writer.write("#######################################################\n");
			System.out.println("HashMap has been written to the file.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<List<Integer>> combinations(int pomRecommendationsSize)
	{		
        List<Integer> numbers = new ArrayList<Integer>();
        
        for(int i=0;i<pomRecommendationsSize;i++)
        {
        	numbers.add(i);
        }
        
        List<List<Integer>> combinations = new ArrayList<>();

        // Generate all possible combinations
        for (int i = 1; i <= numbers.size(); i++) {
            combine(numbers, i, 0, new ArrayList<>(), combinations);
        }

        // Print all combinations
//        for (List<Integer> combination : combinations) {
//            System.out.println(combination);
//        }
        
        return combinations;
	}
	
	private static void combine(List<Integer> numbers, int length, int start, List<Integer> current, List<List<Integer>> combinations)
	{
        if (current.size() == length) {
            combinations.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < numbers.size(); i++) {
            current.add(numbers.get(i));
            combine(numbers, length, i + 1, current, combinations);
            current.remove(current.size() - 1);  // backtrack
        }
    }
}
