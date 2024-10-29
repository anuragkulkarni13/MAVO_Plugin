package common.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DependencyDTO {

	String groupId;
    String artifactId;
    String version;
    String type;
    List<VulnerabilityDTO> vulnerabilities;
    List<DependencyDTO> children;

    public DependencyDTO() {
		super();
		this.vulnerabilities = new ArrayList<VulnerabilityDTO>();
		this.children = new ArrayList<DependencyDTO>();
	}

	public DependencyDTO(String groupId, String artifactId, String version, String type, VulnerabilityDTO vulnerability,
			List<DependencyDTO> children) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.type = type;
		this.vulnerabilities = new ArrayList<VulnerabilityDTO>();
		this.children = new ArrayList<DependencyDTO>();
	}

	public void updateVulberabilities(Map<String, List<VulnerabilityDTO>> vulnerabilityMap) {
//		System.out.println("inside update vulnerabilities");
        if (this == null) return;

        // Stack to hold nodes to visit
        Stack<DependencyDTO> stack = new Stack<>();
        stack.push(this);

        while (!stack.isEmpty()) {
            // Pop the top item from the stack
            DependencyDTO current = stack.pop();

            // Process the current node (you can replace this with any operation you need)
            String jarName = current.getArtifactId()+"-"+current.getVersion()+"."+current.getType();
//            System.out.println(jarName);
            if(vulnerabilityMap.containsKey(jarName))
            {
                List<VulnerabilityDTO> vulnerabilities = vulnerabilityMap.get(jarName);
//                System.out.println(vulnerabilities.size()); 
                current.setVulnerabilities(vulnerabilities);
            }


            // Push all children of the current node onto the stack
            if (current.getChildren() != null) {
                for (DependencyDTO child : current.getChildren()) {
                    stack.push(child);
                }
            }
        }
	}
	
	public String getGroupId() {
		return groupId;
	}
	
    public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
    public String getArtifactId() {
		return artifactId;
	}
	
    public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	
    public String getVersion() {
		return version;
	}
	
    public void setVersion(String version) {
		this.version = version;
	}
	
    public String getType() {
		return type;
	}
	
    public void setType(String type) {
		this.type = type;
	}

	public List<VulnerabilityDTO> getVulnerabilities() {
		return vulnerabilities;
	}

	public void setVulnerabilities(List<VulnerabilityDTO> vulnerabilities) {
		this.vulnerabilities = vulnerabilities;
	}

	public List<DependencyDTO> getChildren() {
		return children;
	}

	public void setChildren(List<DependencyDTO> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "DependencyDTO [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", type="
				+ type + ", vulnerabilities=" + vulnerabilities + ", children=" + children + ", getGroupId()="
				+ getGroupId() + ", getArtifactId()=" + getArtifactId() + ", getVersion()=" + getVersion()
				+ ", getType()=" + getType() + ", getVulnerabilities()=" + getVulnerabilities() + ", getChildren()="
				+ getChildren() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
    
	
}
