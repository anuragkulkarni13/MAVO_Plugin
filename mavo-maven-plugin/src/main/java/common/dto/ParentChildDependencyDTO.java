package common.dto;

import java.util.ArrayList;
import java.util.List;

public class ParentChildDependencyDTO {

	POMDependencyDTO parentDependency;
	List<POMDependencyDTO> childDependency;
	
	public ParentChildDependencyDTO() {
		super();
		childDependency = new ArrayList<>();
	}
	
	public ParentChildDependencyDTO(POMDependencyDTO parentDependency, List<POMDependencyDTO> childDependency) {
		super();
		this.parentDependency = parentDependency;
		this.childDependency = childDependency;
	}

	public POMDependencyDTO getParentDependency() {
		return parentDependency;
	}

	public void setParentDependency(POMDependencyDTO parentDependency) {
		this.parentDependency = parentDependency;
	}

	public List<POMDependencyDTO> getChildDependency() {
		return childDependency;
	}

	public void setChildDependency(List<POMDependencyDTO> childDependency) {
		this.childDependency = childDependency;
	}

	@Override
	public String toString() {
		return "ParentChildDependencyDTO [parentDependency=" + parentDependency + ", childDependency=" + childDependency
				+ ", getParentDependency()=" + getParentDependency() + ", getChildDependency()=" + getChildDependency()
				+ ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString()
				+ "]";
	}
	
}
