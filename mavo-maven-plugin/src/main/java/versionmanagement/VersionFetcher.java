package versionmanagement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import common.Constants;
import common.dto.VersionDTO;

public class VersionFetcher {

	public static long getTimeStampforDependency(String groupId, String artifactId, String currentVersion)
	{
		long timestamp = 0L;
		String paginatedUrl = "https://search.maven.org/solrsearch/select?q=g:%22"+groupId+"%22+AND+a:%22"+artifactId+"%22+AND+v:"+currentVersion+"&rows=100&core=gav&wt=json";
		try {
			String jsonResponse = fetchJsonResponse(paginatedUrl);
			if (jsonResponse != null) {
				List<VersionDTO> versions = parseVersions(jsonResponse);
				if(versions.size()>0)
				{
					timestamp = versions.get(0).getTimestamp();					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timestamp;
	}
	
	public static List<VersionDTO> fetchAllVersions(String groupId, String artifactId, String currentVersion, String patchRestriction){
		List<VersionDTO> versionList = new ArrayList<>();
		int start = 0;  // Start index for pagination
		boolean moreResults = true;  // Flag to check if more results exist

		while (moreResults) {
			String paginatedUrl = "https://search.maven.org/solrsearch/select?q=g:%22"+groupId+"%22+AND+a:%22"+artifactId+"%22&rows=100&core=gav&wt=json&start=" + start;
			if(patchRestriction.equalsIgnoreCase("major"))
			{
				String majorPathVersion = currentVersion.split("\\.")[0];
//				System.out.println(majorPathVersion);
				paginatedUrl = "https://search.maven.org/solrsearch/select?q=g:%22"+groupId+"%22+AND+a:%22"+artifactId+"%22+AND+v:"+majorPathVersion+"*&rows=100&core=gav&wt=json&start=" + start;
			}
			else if(patchRestriction.equalsIgnoreCase("minor"))
			{
				String majorPathVersion = currentVersion.split("\\.")[0]+"."+currentVersion.split("\\.")[1];
//				System.out.println(majorPathVersion);
				paginatedUrl = "https://search.maven.org/solrsearch/select?q=g:%22"+groupId+"%22+AND+a:%22"+artifactId+"%22+AND+v:"+majorPathVersion+"*&rows=100&core=gav&wt=json&start=" + start;
			}
			else if(patchRestriction.equalsIgnoreCase("release"))
			{
				String majorPathVersion = currentVersion.split("\\.")[0]+"."+currentVersion.split("\\.")[1];
				if(currentVersion.split("\\.").length>2)
				{
					majorPathVersion = currentVersion.split("\\.")[0]+"."+currentVersion.split("\\.")[1]+"."+currentVersion.split("\\.")[2];
				}
//				System.out.println(majorPathVersion);
				paginatedUrl = "https://search.maven.org/solrsearch/select?q=g:%22"+groupId+"%22+AND+a:%22"+artifactId+"%22+AND+v:"+majorPathVersion+"*&rows=100&core=gav&wt=json&start=" + start;
			}
			try {
				String jsonResponse = fetchJsonResponse(paginatedUrl);
				if (jsonResponse != null) {
					List<VersionDTO> versions = parseVersions(jsonResponse);
					versionList.addAll(versions);

					// Check if more results exist
					moreResults = hasMoreResults(jsonResponse);
					start += Constants.ROWS_PER_PAGE;  // Increment the start index for the next page
				} else {
					moreResults = false;  // Stop if there was an issue with the request
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		return versionList;
	}

	private static String fetchJsonResponse(String apiUrl) throws Exception {
		URL url = new URL(apiUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(10000);  // 10 seconds for connection timeout
		connection.setReadTimeout(10000);     // 10 seconds for read timeout

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuilder content = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			return content.toString();
		} else {
			System.out.println("Error: " + connection.getResponseMessage());
			return null;
		}
	}

	// Method to parse the JSON response and extract versions and timestamps

	private static List<VersionDTO> parseVersions(String jsonResponse) {
		List<VersionDTO> versionList = new ArrayList<>();
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

		// Navigate to the response.docs array
		JsonObject responseObj = jsonObject.getAsJsonObject("response");
		JsonArray docsArray = responseObj.getAsJsonArray("docs");

		// Iterate through the docs array to extract version and timestamp
		for (JsonElement docElement : docsArray) {
			JsonObject docObj = docElement.getAsJsonObject();
			String version = docObj.get("v").getAsString();
			long timestamp = docObj.get("timestamp").getAsLong();
			versionList.add(new VersionDTO(version, timestamp));
		}
		return versionList;
	}

	// Method to check if more results exist in the JSON response
	
	private static boolean hasMoreResults(String jsonResponse) {
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

		// Get the total number of results and the number of results retrieved so far
		JsonObject responseObj = jsonObject.getAsJsonObject("response");
		int numFound = responseObj.get("numFound").getAsInt();  // Total number of results
		int start = responseObj.get("start").getAsInt();  // Start index of current page
		int rows = Constants.ROWS_PER_PAGE;  // Number of results per page

		// If the start index plus the number of rows is less than the total number of results, more pages exist
		return (start + rows) < numFound;
	}


}
