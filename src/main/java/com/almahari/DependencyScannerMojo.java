package com.almahari;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "scan", defaultPhase = LifecyclePhase.COMPILE)

public class DependencyScannerMojo extends BaseMojo {

    Set<ProjectDependency> projectDependencies = new HashSet<>();
    Set<String> parsedPom = new HashSet<>();
    Map<String, String> versions = new HashMap<>();
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;
    @Parameter(required = true)
    String localRepository;
    @Parameter(defaultValue = "true")
    Boolean parsePlugins;
    @Parameter(defaultValue = "true")
    Boolean parseDependencies;

    public void execute() throws MojoExecutionException {

        String projectPomFilePath = project.getFile()
                                           .getAbsolutePath();

        logV("Local Repository Path: " + localRepository);
        logV("Path of pom.xml: " + projectPomFilePath);
        logV("Parse plugins: " + parsePlugins);
        logV("Parse dependencies: " + parseDependencies);

        logDashes("Scanning");
        loadProjectDetails();
        parsePomFile(projectDependencies, projectPomFilePath);

        printDependencies(0, projectDependencies);
    }

    private void printDependencies(int level, Set<ProjectDependency> projectDependencies) {

        String indent = level == 0 ? "" : (String.format("%0" + level + "d", 0)
                                                 .replace("0", "|----"));

        for (ProjectDependency dependency : projectDependencies) {

            if (dependency.isValidFile()) {
                logBlue(indent + (verbose <= 1 ? dependency.getId() : dependency.toString()));
            } else {
                logRed(indent + (verbose <= 1 ? dependency.getId() : dependency.toString()) + " [Error while parsing the pom file]");
            }

            if (dependency.hasDependencies()) {
                printDependencies(level + 1, dependency.getDependencies());
            }
        }
    }

    private boolean parsePomFile(Set<ProjectDependency> dependencies, String pomFilePath) {
        logV("Scanning:" + pomFilePath);

        try {
            if (parsedPom.contains(pomFilePath)) {
                logVV("Pom already parsed");
                return true;
            }

            File inputFile = new File(pomFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement()
               .normalize();

            parsedPom.add(pomFilePath);
            logVV("parsed files :" + parsedPom.size());

            if (parsePlugins)
                parsePomPlugins(dependencies, doc);

            if (parseDependencies)
                parsePomDependencies(dependencies, doc);

            return true;
        } catch (Exception e) {
            if (verbose >= 2) {
                error("File Not found: ", e);
            } else if (verbose == 1) {
                logRed("File Not found: " + e.getMessage());
            }
        }

        return false;
    }

    private void parsePomPlugins(Set<ProjectDependency> dependencies, Document doc) {
        NodeList nodeList = doc.getElementsByTagName("plugin");
        parseNode(nodeList, "pluginManagement", dependencies);
    }

    private void parsePomDependencies(Set<ProjectDependency> dependencies, Document doc) {
        NodeList nodeList = doc.getElementsByTagName("dependency");
        parseNode(nodeList, "dependencyManagement", dependencies);
    }

    private void parseNode(NodeList nodeList, String managementTag, Set<ProjectDependency> dependencies) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (!managementTag.equals(node.getParentNode()
                                          .getParentNode()
                                          .getNodeName())) {

                ProjectDependency dependency = new ProjectDependency(node);
                dependency.setProjectVersion(getDependencyVersion(dependency));
                dependency.setType(managementTag.replace("Management", ""));
                boolean validFile = parsePomFile(dependency.getDependencies(), Paths.get(localRepository, dependency.getPomPath())
                                                                                    .toString());
                dependency.setIsValidFile(validFile);
                dependencies.add(dependency);

            }
        }
    }

    private String getDependencyVersion(ProjectDependency dependency) {

        String key = dependency.getGroupArtifact();
        if (versions.containsKey(key)) {
            return versions.get(key);
        }

        return "NA";
    }

    private void loadProjectDetails() {

        List<Plugin> buildPlugins = project.getBuildPlugins();
        List<Dependency> dependencies = project.getDependencies();

        versions.putAll(buildPlugins
                .stream()
                .collect(Collectors.toMap(d -> d.getGroupId() + ":" + d.getArtifactId(), Plugin::getVersion)));

        versions.putAll(dependencies
                .stream()
                .collect(Collectors.toMap(d -> d.getGroupId() + ":" + d.getArtifactId(), Dependency::getVersion)));
    }

}
