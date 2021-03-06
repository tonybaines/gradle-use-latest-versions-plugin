package se.patrikerdes

import org.gradle.testkit.runner.BuildResult

class VariableUpdatesFunctionalTest extends BaseFunctionalTest {
    void "an outdated module dependency based on a variable can be updated, single quotes"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            def junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
    }

    void "an outdated module dependency based on a variable can be updated, double quotes"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            def junit_version = "4.0"
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = \"$CurrentVersions.JUNIT\"")
    }

    void "an outdated module dependency based on a variable can be updated, plus"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            def junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:"+ junit_version
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
    }

    void "an outdated map notation module dependency based on a variable can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
            
            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            def junit_version = "4.0"
            
            dependencies {
                testCompile group: 'junit', name: 'junit', version: junit_version
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("junit_version = \"$CurrentVersions.JUNIT\"")
    }

    void "Extra properties extensions can be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }
            
            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            ext.junit_version = '4.0'
            
            dependencies {
                testCompile group: 'junit', name: 'junit', version: junit_version
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains("ext.junit_version = '$CurrentVersions.JUNIT'")
    }

    // It might make sense to update these expressions as well, but this is the current behavior
    void "an expression with \${x} won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            def commons = "1.0"
            
            dependencies {
                compile "commons-lang:commons-lang:\${commons}"
                compile "commons-logging:commons-logging:\${"1.0"}"
            }
        """

        when:
        useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('commons = "1.0"')
        updatedBuildFile.contains('${commons}')
        updatedBuildFile.contains('${"1.0"}')
    }

    void "a variable used for multiple dependencies with different latest versions won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            def commons = "1.0"
            
            dependencies {
                compile "commons-lang:commons-lang:\$commons"
                compile "commons-logging:commons-logging:\$commons"
            }
        """

        when:
        BuildResult result = useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('commons = "1.0"')
        result.output.contains('A problem was detected')
    }

    void "a variable for deps with different latest versions where one dependency is current won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            def commons = "1.2"
            
            dependencies {
                compile "commons-codec:commons-codec:\$commons"       // Latest version: 1.2
                compile "commons-logging:commons-logging:\$commons"   // Latest version: 1.11
            }
        """

        when:
        BuildResult result = useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        updatedBuildFile.contains('commons = "1.2"')
        result.output.contains('A problem was detected')
    }

    void "a variable defined more than once won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            def junit_version = '3.7'

            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            junit_version = '4.0'
            
            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """

        when:
        BuildResult result = useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')

        then:
        !updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
        result.output.contains('A problem was detected')
    }

    void "a variable assigned to in more than one file won't be updated"() {
        given:
        buildFile << """
            plugins {
                id 'se.patrikerdes.use-latest-versions'
                id 'com.github.ben-manes.versions' version '$CurrentVersions.VERSIONS'
            }

            project.ext.junit_version = '3.7'

            apply from: 'second.gradle'

            apply plugin: 'java'

            repositories {
                mavenCentral()
            }

            dependencies {
                testCompile "junit:junit:\$junit_version"
            }
        """
        File secondFile = testProjectDir.newFile('second.gradle')
        secondFile << """
            rootProject.junit_version = '4.0'
        """

        when:
        BuildResult result = useLatestVersions()
        String updatedBuildFile = buildFile.getText('UTF-8')
        String updatedSecondFile = secondFile.getText('UTF-8')

        then:
        !updatedBuildFile.contains("junit_version = '$CurrentVersions.JUNIT'")
        !updatedSecondFile.contains("junit_version = '$CurrentVersions.JUNIT'")
        result.output.contains('A problem was detected')
    }
}
