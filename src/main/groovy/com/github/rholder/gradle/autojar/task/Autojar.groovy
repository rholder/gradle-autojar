/*
 * Copyright 2013 Ray Holder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rholder.gradle.autojar.task

import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.internal.file.IdentityFileResolver
import org.gradle.api.java.archives.Manifest
import org.gradle.api.java.archives.internal.DefaultManifest
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.bundling.Jar
import org.gradle.util.ConfigureUtil

class Autojar extends JavaExec implements PublishArtifact {

    Logger logger
    Jar baseJar
    Manifest manifest
    ExtractAutojar extractAutojar

    Configuration targetConfiguration

    String mainClass
    List<String> classes    // convert these to raw files
    List<String> files      // all the class files, etc.

    File autojarBuildDir
    String autojarExtra     // default to -bav
    String autojarClasspath // -c all dependencies, including base jar
    String autojarManifest  // path to final manifest file -m
    File autojarOutput      // -o path to output file

    // publish artifact overrides
    Date publishDate
    String publishClassifier
    String publishType
    String publishExtension

    Autojar() {
        logger = project.logger
        group = "Autojar"
        description = "Create an Autojar runnable archive from the current project using a given main class."

        autojarBuildDir = project.ext.autojarBuildDir
        extractAutojar = project.tasks.extractAutojar

        // default to main project jar if none is specified
        baseJar = baseJar ?: project.tasks.jar

        dependsOn = [baseJar, extractAutojar]

        manifest = new DefaultManifest(new IdentityFileResolver())

        // default to -ba if none is specified
        autojarExtra = autojarExtra ?: "-ba"

        // default to runtime configuration if none is specified
        targetConfiguration = targetConfiguration ?: project.configurations.runtime

        // munge the classpath
        autojarClasspath = baseJar.getArchivePath().absolutePath
        def libs = targetConfiguration.resolve()
        libs.each {
            logger.debug("Including dependency: " + it.absolutePath)
            autojarClasspath += ":" + it.absolutePath
        }

        autojarOutput = new File(autojarBuildDir, generateFilename(baseJar, "autojar"))

        inputs.files([baseJar.getArchivePath().absoluteFile, extractAutojar.extractedFile])
        outputs.file(autojarOutput)
    }

    /**
     * Allow configuration view of a 'manifest' closure similar to the Jar task.
     *
     * @param configureClosure target closure
     */
    Autojar manifest(Closure configureClosure) {
        manifest = manifest ?: new DefaultManifest(project.fileResolver)
        ConfigureUtil.configure(configureClosure, manifest);
        return this;
    }

    @Override
    public void exec() {
        setMain('org.sourceforge.autojar.Autojar')
        classpath(extractAutojar.extractedFile.absolutePath)

        files = files ?: []

        // convert class notation to raw files for Autojar
        classes = classes ?: []
        classes.each {
            files.add(it.replaceAll("\\.", "/") + ".class")
        }

        // infer main class starting point from manifest if it exists
        if(mainClass) {
            files.add(mainClass.replaceAll("\\.", "/") + ".class")
            manifest.attributes.put('Main-Class', mainClass)
        }

        // by now we should have at least one file
        if(!files) {
            throw new InvalidUserDataException("No files set in autojarFiles and no main class to infer.")
        }

        autojarManifest = writeJarManifestFile(manifest).absolutePath

        def autojarArgs = [autojarExtra, "-c", autojarClasspath, "-m", autojarManifest,"-o", autojarOutput]
        autojarArgs.addAll(files)
        logger.info('{}', autojarArgs)

        args(autojarArgs)
        super.exec()
    }

    @Override
    String getExtension() {
        return publishExtension ?: Jar.DEFAULT_EXTENSION
    }

    @Override
    String getType() {
        return publishType ?: Jar.DEFAULT_EXTENSION
    }

    @Override
    String getClassifier() {
        return publishClassifier ?: 'autojar'
    }

    @Override
    File getFile() {
        return autojarOutput
    }

    @Override
    Date getDate() {
        return publishDate ?: new Date(autojarOutput.lastModified())
    }

    @Override
    TaskDependency getBuildDependencies() {
        return getTaskDependencies()
    }

    /**
     * Return a manifest configured to boot the jar using One-JAR and then
     * passing over control to the configured main class.
     */
    static File writeJarManifestFile(Manifest manifest) {
        File manifestFile = File.createTempFile("manifest", ".mf")
        manifestFile.deleteOnExit()

        manifestFile.withWriter { writer ->
            manifest.writeTo(writer)
        }

        // hack to clip off the first line, Manifest-Version, in the file because Autojar is weird
        def lines = manifestFile.readLines()
        lines.remove(0)
        manifestFile.setText(lines.join("\n"))

        return manifestFile
    }

    /**
     * This is kind of a hack to ensure we get "-classifier.jar" tacked on to
     * archiveName + a valid version.
     */
    static String generateFilename(Jar jar, String classifier) {
        return jar.archiveName - ("." + jar.extension) + "-" + classifier + "." + jar.extension
    }
}
