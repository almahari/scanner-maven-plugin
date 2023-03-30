package com.almahari;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ProjectDependency {
    private static final String separator = System.getProperty("file.separator");
    private String groupId = "";
    private String artifactId = "";
    private String version = "";
    private String projectVersion = "";
    private String scope = "";
    private boolean highlighted = false;

    private String type = "";

    private boolean validFile = false;

    private Set<ProjectDependency> dependencies = new HashSet<>();

    public ProjectDependency(Node xmlNode) {
        NodeList childNodes = xmlNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);

            switch (item.getNodeName()) {
                case "groupId":
                    this.groupId = item.getTextContent()
                                       .trim();
                    break;
                case "artifactId":
                    this.artifactId = item.getTextContent()
                                          .trim();
                    break;
                case "version":
                    this.version = item.getTextContent()
                                       .trim();
                    break;
            }
        }
    }

    public String getId() {
        return String.format("%s:%s:%s (%s)", groupId, artifactId, getVersion(), type);
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getVersion() {

        if(!projectVersion.equals(""))
        {
            return projectVersion;
        }

        return version;

//        if (version != null && !version.equals("") && !version.startsWith("$")) {
//            return version;
//        }
//
//        return projectVersion;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<ProjectDependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<ProjectDependency> dependencies) {
        this.dependencies = dependencies;
    }

    public String getPomPath() {
        String v = getVersion();
        return Paths.get(groupId.replace(".", separator), artifactId, v, artifactId + "-" + v + ".pom")
                    .toString();
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String toString() {
        return "ProjectDependency{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", projectVersion='" + projectVersion + '\'' +
                ", scope='" + scope + '\'' +
                ", highlighted=" + highlighted +
                ", type='" + type + '\'' +
                ", dependencies=" + dependencies +
                '}';
    }

    public boolean hasDependencies() {
        return this.dependencies.size() > 0;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setIsValidFile(boolean validFile) {
        this.validFile = validFile;
    }

    public boolean isValidFile() {
        return validFile;
    }

    public String getGroupArtifact() {
        return String.format("%s:%s", groupId, artifactId);
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
