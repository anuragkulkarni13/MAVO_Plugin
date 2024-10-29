package common.dto;

public class VersionDTO {

	String version;
	long timestamp;
	
	public VersionDTO() {
		super();
	}

	public VersionDTO(String version, long timestamp) {
		super();
		this.version = version;
		this.timestamp = timestamp;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "VersionDTO [version=" + version + ", timestamp=" + timestamp + ", getVersion()=" + getVersion()
				+ ", getTimestamp()=" + getTimestamp() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}
	
}
