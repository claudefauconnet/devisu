package com.fauconnet.old;

public class Item {
	
	 public String getTechnologyName() {
		return TechnologyName;
	}
	public void setTechnologyName(String technologyName) {
		TechnologyName = technologyName;
	}
	public String getDomainName() {
		return DomainName;
	}
	public void setDomainName(String domainName) {
		DomainName = domainName;
	}
	public String getTechnologyId() {
		return TechnologyId;
	}
	public void setTechnologyId(String technologyId) {
		TechnologyId = technologyId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		id = id;
	}
	public String getPriority() {
		return Priority;
	}
	public void setPriority(String priority) {
		Priority = priority;
	}
	public String getMatureness() {
		return Matureness;
	}
	public void setMatureness(String matureness) {
		Matureness = matureness;
	}
	public String getImpact() {
		return Impact;
	}
	public void setImpact(String impact) {
		Impact = impact;
	}
	public String getTerm() {
		return Term;
	}
	public void setTerm(String term) {
		Term = term;
	}
	public String getUsageClientValue() {
		return UsageClientValue;
	}
	public void setUsageClientValue(String usageClientValue) {
		UsageClientValue = usageClientValue;
	}
	public String getUsageMobilityValue() {
		return UsageMobilityValue;
	}
	public void setUsageMobilityValue(String usageMobilityValue) {
		UsageMobilityValue = usageMobilityValue;
	}
	public String getUsageOfficealue() {
		return UsageOfficealue;
	}
	public void setUsageOfficealue(String usageOfficealue) {
		UsageOfficealue = usageOfficealue;
	}
	public String getUsageFactoryValue() {
		return UsageFactoryValue;
	}
	public void setUsageFactoryValue(String usageFactoryValue) {
		UsageFactoryValue = usageFactoryValue;
	}
	public float getX() {
		return X;
	}
	public void setX(float x) {
		X = x;
	}
	public float getY() {
		return Y;
	}
	public void setY(float y) {
		Y = y;
	}
	public String getCoordinatesComments() {
		return CoordinatesComments;
	}
	public void setCoordinatesComments(String coordinatesComments) {
		CoordinatesComments = coordinatesComments;
	}
	public String getComments() {
		return Comments;
	}
	public void setComments(String comments) {
		Comments = comments;
	}
	public String getBranchValue() {
		return BranchValue;
	}
	public void setBranchValue(String branchValue) {
		BranchValue = branchValue;
	}
	public String getYear() {
		return Year;
	}
	public void setYear(String year) {
		Year = year;
	}
	public String getWikiLink() {
		return WikiLink;
	}
	public void setWikiLink(String wikiLink) {
		WikiLink = wikiLink;
	}
	public boolean getPermissionEdit() {
		return PermissionEdit;
	}
	public void setPermissionEdit(boolean permissionEdit) {
		PermissionEdit = permissionEdit;
	}
	public String getVersion() {
		return Version;
	}
	public void setVersion(String version) {
		Version = version;
	}
	public String getVideoLink() {
		return VideoLink;
	}
	public void setVideoLink(String videoLink) {
		VideoLink = videoLink;
	}
	 public String getAction() {
			return Action;
		}
		public void setAction(String action) {
			Action = action;
		}
		public String getGroup() {
			return Group;
		}
		public void setGroup(String group) {
			Group = group;
		}
	 String TechnologyName="";
	String DomainName="";
	 String TechnologyId="";
	 public String Id="";
	 int id=-1;

	 String Priority="";
	 String Matureness="";
	 String Impact="";
	 String Term="";
	 String UsageClientValue="";
	 String UsageMobilityValue="";
	 String UsageOfficealue="";
	 String UsageFactoryValue="";
	 float X=0;
	 float Y=0;
	 String CoordinatesComments="";
	 String Comments="";
	 String BranchValue="";
	 String Year="";
	 String WikiLink="";
	 boolean  PermissionEdit=false;
	 String Version="";
	 String VideoLink="";
	 
	 String Action="loadJSON";
	String Group="{eNovation}{POT Owners}";

}
