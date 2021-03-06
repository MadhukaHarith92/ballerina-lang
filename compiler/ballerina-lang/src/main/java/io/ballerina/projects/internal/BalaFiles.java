/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.projects.internal;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.bala.DependencyGraphJson;
import io.ballerina.projects.internal.bala.ModuleDependency;
import io.ballerina.projects.internal.model.Dependency;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.DependencyGraph.DependencyGraphBuilder.getBuilder;
import static io.ballerina.projects.internal.ProjectFiles.loadDocuments;
import static io.ballerina.projects.util.ProjectConstants.BALA_DOCS_DIR;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCY_GRAPH_JSON;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_JSON;

/**
 * Contains a set of utility methods that create an in-memory representation of a Ballerina project using a bala.
 *
 * @since 2.0.0
 */
public class BalaFiles {
    private static final Gson gson = new Gson();

    // TODO change class name to utils
    private BalaFiles() {
    }

    static PackageData loadPackageData(Path balaPath, PackageManifest packageManifest) {
        URI zipURI = URI.create("jar:" + balaPath.toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            // Load default module
            String pkgName = packageManifest.name().toString();
            Path packageRoot = zipFileSystem.getPath("/");
            ModuleData defaultModule = loadModule(pkgName, pkgName, packageRoot);
            DocumentData packageMd = loadDocument(zipFileSystem.getPath(BALA_DOCS_DIR)
                    .resolve(ProjectConstants.PACKAGE_MD_FILE_NAME));
            // load other modules
            List<ModuleData> otherModules = loadOtherModules(pkgName, packageRoot);
            return PackageData.from(balaPath, defaultModule, otherModules, null, null, null, packageMd);
        } catch (IOException e) {
            throw new ProjectException("Failed to read bala file:" + balaPath);
        }
    }

    private static void validatePackageJson(PackageJson packageJson, Path balaPath) {
        if (packageJson.getOrganization() == null || "".equals(packageJson.getOrganization())) {
            throw new ProjectException("'organization' does not exists in 'package.json': " + balaPath);
        }
        if (packageJson.getName() == null || "".equals(packageJson.getName())) {
            throw new ProjectException("'name' does not exists in 'package.json': " + balaPath);
        }
        if (packageJson.getVersion() == null || "".equals(packageJson.getVersion())) {
            throw new ProjectException("'version' does not exists in 'package.json': " + balaPath);
        }
    }

    public static DocumentData loadDocument(Path documentFilePath) {
        if (Files.notExists(documentFilePath)) {
            return null;
        } else {
            String content;
            try {
                content = Files.readString(documentFilePath, Charset.defaultCharset());
            } catch (IOException e) {
                throw new ProjectException(e);
            }
            return DocumentData.from(Optional.of(documentFilePath.getFileName()).get().toString(), content);
        }
    }

    private static ModuleData loadModule(String pkgName, String fullModuleName,  Path packagePath) {
        Path modulePath = packagePath.resolve(MODULES_ROOT).resolve(fullModuleName);
        Path moduleDocPath = packagePath.resolve(BALA_DOCS_DIR).resolve(MODULES_ROOT).resolve(fullModuleName);
        // check module path exists
        if (Files.notExists(modulePath)) {
            throw new ProjectException("The 'modules' directory does not exists in '" + modulePath + "'");
        }

        String moduleName = fullModuleName;
        if (!pkgName.equals(moduleName)) {
            // not default module
            moduleName = fullModuleName.substring(pkgName.length() + 1);
        }

        // validate moduleName
        if (!ProjectUtils.validateModuleName(moduleName)) {
            throw new ProjectException("Invalid module name : '" + moduleName + "' :\n" +
                    "Module name can only contain alphanumerics, underscores and periods " +
                    "and the maximum length is 256 characters: " + modulePath);
        }

        List<DocumentData> srcDocs = loadDocuments(modulePath);
        List<DocumentData> testSrcDocs = Collections.emptyList();
        DocumentData moduleMd = loadDocument(moduleDocPath.resolve(ProjectConstants.MODULE_MD_FILE_NAME));

        return ModuleData.from(modulePath, moduleName, srcDocs, testSrcDocs, moduleMd);
    }

    private static List<ModuleData> loadOtherModules(String pkgName, Path packagePath) {
        Path modulesDirPath = packagePath.resolve(MODULES_ROOT);
        try (Stream<Path> pathStream = Files.walk(modulesDirPath, 1)) {
            return pathStream
                    .filter(path -> !path.equals(modulesDirPath))
                    .filter(path -> path.getFileName() != null
                            && !path.getFileName().toString().equals(pkgName))
                    .filter(Files::isDirectory)
                    .map(modulePath -> modulePath.getFileName().toString())
                    .map(fullModuleName -> loadModule(pkgName, fullModuleName, packagePath))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProjectException("Failed to read modules from directory: " + modulesDirPath, e);
        }
    }

    public static PackageManifest createPackageManifest(Path balaPath) {
        try {
            return createManifest(balaPath);
        } catch (IOException e) {
            throw new ProjectException("Failed to read bala file:" + balaPath);
        }
    }

    public static PackageManifest createManifest(Path balaPath) throws IOException {
        URI zipURI = URI.create("jar:" + balaPath.toAbsolutePath().toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            return readFromArchive(zipFileSystem, balaPath);
        } catch (FileAlreadyExistsException e) {
            try (FileSystem zipFileSystem = FileSystems.getFileSystem(zipURI)) {
                return readFromArchive(zipFileSystem, balaPath);
            }
        }
    }

    private static PackageManifest readFromArchive(FileSystem zipFileSystem, Path balaPath) {
        Path packageJsonPath = zipFileSystem.getPath(PACKAGE_JSON);
        if (Files.notExists(packageJsonPath)) {
            throw new ProjectException("package.json does not exists in '" + balaPath + "'");
        }

        // Load `package.json`
        PackageJson packageJson = readPackageJson(balaPath, packageJsonPath);
        validatePackageJson(packageJson, balaPath);
        extractPlatformLibraries(zipFileSystem, packageJson, balaPath);
        return getPackageManifest(packageJson);
    }

    static DependencyGraphResult createPackageDependencyGraph(Path balaPath, String packageName) {
        URI zipURI = URI.create("jar:" + balaPath.toAbsolutePath().toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path dependencyGraphJsonPath = zipFileSystem.getPath(DEPENDENCY_GRAPH_JSON);
            if (Files.notExists(dependencyGraphJsonPath)) {
                throw new ProjectException(DEPENDENCY_GRAPH_JSON + " does not exists in '" + balaPath + "'");
            }

            // Load `dependency-graph.json`
            DependencyGraphJson dependencyGraphJson = readDependencyGraphJson(balaPath, dependencyGraphJsonPath);

            DependencyGraph<PackageDescriptor> packageDependencyGraph = createPackageDependencyGraph(
                    dependencyGraphJson.getPackageDependencyGraph());
            Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDescriptorListMap = createModuleDescDependencies(
                    dependencyGraphJson.getModuleDependencies());

            return new DependencyGraphResult(packageDependencyGraph, moduleDescriptorListMap);

        } catch (IOException e) {
            throw new ProjectException("Failed to read bala file:" + balaPath);
        }
    }

    private static void extractPlatformLibraries(FileSystem zipFileSystem, PackageJson packageJson, Path balaPath) {
        if (packageJson.getPlatformDependencies() == null) {
            return;
        }
        packageJson.getPlatformDependencies().forEach(dependency -> {
            Path libPath = balaPath.getParent().resolve(dependency.getPath());
            if (!Files.exists(libPath)) {
                try {
                    Files.createDirectories(libPath.getParent());
                    Files.copy(zipFileSystem.getPath(dependency.getPath()), libPath);
                } catch (IOException e) {
                    throw new ProjectException("Failed to extract platform dependency:" + libPath.getFileName(), e);
                }
            }
            dependency.setPath(libPath.toString());
        });
    }

    private static PackageManifest getPackageManifest(PackageJson packageJson) {
        PackageDescriptor pkgDesc = PackageDescriptor.from(PackageOrg.from(packageJson.getOrganization()),
                PackageName.from(packageJson.getName()), PackageVersion.from(packageJson.getVersion()));
        List<PackageManifest.Dependency> dependencies;
        if (packageJson.getDependencies() != null) {
            dependencies = packageJson.getDependencies();
        } else {
            dependencies = Collections.emptyList();
        }

        Map<String, PackageManifest.Platform> platforms = new HashMap<>(Collections.emptyMap());
        if (packageJson.getPlatformDependencies() != null) {
            List<Map<String, Object>> platformDependencies = new ArrayList<>();
            packageJson.getPlatformDependencies().forEach(dependency -> {
                String jsonStr = gson.toJson(dependency);
                platformDependencies.add(gson.fromJson(jsonStr, Map.class));

            });
            PackageManifest.Platform platform = new PackageManifest.Platform(platformDependencies);
            platforms.put(packageJson.getPlatform(), platform);
        }

        return PackageManifest.from(pkgDesc, dependencies, platforms);
    }

    private static PackageJson readPackageJson(Path balaPath, Path packageJsonPath) {
        PackageJson packageJson;
        try {
            packageJson = gson.fromJson(Files.newBufferedReader(packageJsonPath), PackageJson.class);
        } catch (JsonSyntaxException e) {
            throw new ProjectException("Invalid package.json format in '" + balaPath + "'");
        } catch (IOException | JsonIOException e) {
            throw new ProjectException("Failed to read the package.json in '" + balaPath + "'");
        }
        return packageJson;
    }

    private static DependencyGraph<PackageDescriptor> createPackageDependencyGraph(
            List<Dependency> packageDependencyGraph) {
        DependencyGraph.DependencyGraphBuilder<PackageDescriptor> graphBuilder = getBuilder();

        for (Dependency dependency : packageDependencyGraph) {
            PackageDescriptor pkg = PackageDescriptor.from(PackageOrg.from(dependency.getOrg()),
                    PackageName.from(dependency.getName()), PackageVersion.from(dependency.getVersion()));
            Set<PackageDescriptor> dependentPackages = new HashSet<>();
            for (Dependency dependencyPkg : dependency.getDependencies()) {
                dependentPackages.add(PackageDescriptor.from(PackageOrg.from(dependencyPkg.getOrg()),
                        PackageName.from(dependencyPkg.getName()),
                        PackageVersion.from(dependencyPkg.getVersion())));
            }
            graphBuilder.addDependencies(pkg, dependentPackages);
        }

        return graphBuilder.build();
    }

    private static Map<ModuleDescriptor, List<ModuleDescriptor>> createModuleDescDependencies(
            List<ModuleDependency> modDepEntries) {
        return modDepEntries.stream()
                .collect(Collectors.toMap(BalaFiles::getModuleDescriptorFromDependencyEntry,
                        modDepEntry -> createModDescriptorList(modDepEntry.getDependencies())));
    }

    private static ModuleDescriptor getModuleDescriptorFromDependencyEntry(ModuleDependency modDepEntry) {
        PackageDescriptor pkgDesc = PackageDescriptor.from(PackageOrg.from(modDepEntry.getOrg()),
                PackageName.from(modDepEntry.getPackageName()),
                PackageVersion.from(modDepEntry.getVersion()));
        final ModuleName moduleName = ModuleName.from(modDepEntry.getModuleName(), pkgDesc.org());
        return ModuleDescriptor.from(moduleName, pkgDesc);
    }

    private static List<ModuleDescriptor> createModDescriptorList(List<ModuleDependency> modDepEntries) {
        return modDepEntries.stream()
                .map(BalaFiles::getModuleDescriptorFromDependencyEntry)
                .collect(Collectors.toList());
    }

    private static DependencyGraphJson readDependencyGraphJson(Path balaPath, Path dependencyGraphJsonPath) {
        DependencyGraphJson dependencyGraphJson;
        try {
            dependencyGraphJson = gson
                    .fromJson(Files.newBufferedReader(dependencyGraphJsonPath), DependencyGraphJson.class);
        } catch (JsonSyntaxException e) {
            throw new ProjectException("Invalid " + DEPENDENCY_GRAPH_JSON + " format in '" + balaPath + "'");
        } catch (IOException | JsonIOException e) {
            throw new ProjectException("Failed to read the " + DEPENDENCY_GRAPH_JSON + " in '" + balaPath + "'");
        }
        return dependencyGraphJson;
    }

    /**
     * {@code DependencyGraphResult} contains package and module dependency graphs.
     */
    public static class DependencyGraphResult {
        private final DependencyGraph<PackageDescriptor> packageDependencyGraph;
        private final Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDependencies;

        DependencyGraphResult(DependencyGraph<PackageDescriptor> packageDependencyGraph,
                              Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDependencies) {
            this.packageDependencyGraph = packageDependencyGraph;
            this.moduleDependencies = moduleDependencies;
        }

        DependencyGraph<PackageDescriptor> packageDependencyGraph() {
            return packageDependencyGraph;
        }

        public Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDependencies() {
            return moduleDependencies;
        }
    }
}
