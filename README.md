##What is this?
This plugin rolls up your current project's jar, resources, and all of its
dependencies into a self-contained archive with only the minimal set of class
and resource files necessary to the project. It collects all the parameters
based on reasonable defaults from the project and forks off a process to run
`Autojar` with these collected values.

##What is Autojar?
Here's a description of what `Autojar` does from the project page:
```
`Autojar` creates jar archives of minimal size from different sources (classes,
directories, libraries). Starting from one or more classes, it scans the
bytecode recursively for other classes, extracts them from their archives if
necessary, and adds them to the output file. The resulting archive contains only
classes that are really needed. Thus the size and loading time of applets can be
kept low, and applications can be made self-contained.
```
You can read more about `Autojar` [here](http://autojar.sourceforge.net/) as well
as find the original source code.

##Examples

At a minimum, you only need to supply your project's main class and the
`gradle-autojar` plugin will take care of the rest by using it as the starting
point for running the `Autojar` process:
```groovy
apply plugin: 'gradle-autojar'

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.github.rholder:gradle-autojar:1.0.0'
    }
}

task myAwesomeFunJar(type: Autojar) {
    mainClass = 'com.foo.Foo'
}
```

Other available parameters for customizing the behavior:
```groovy
task myAwesomeFunJar(type: Autojar) {
    mainClass = 'com.foo.Foo'

    // manifest closure works just like the Jar task
    manifest {
        attributes('Created-By':'Gradle Autojar')
    }

    // resource files must be manually added, resolved from the classpath for inclusion
    files = ['foo.txt', 'dir/foo2.txt']

    // additional classes can be manually added when they're not included automatically
    classes = ['org.apache.commons.lang.time.DateUtils']

    // override the defaults of -ba
    autojarExtra = '-bav'
}
```

##License
The `gradle-autojar` build plugin is released under version 2.0 of the [Apache
License](http://www.apache.org/licenses/LICENSE-2.0). This plugin comes bundled
with an unmodified release of `Autojar`, which is licensed under the terms set
forth by the GPLv2. In compliance with the use of GPLv2 licensed software,
`gradle-autojar` only utilizes a fork and exec mechanism for running an
unmodified `Autojar` process whereby derived arguments are passed from the
plugin. The original source code for `Autojar` can be found [here](http://autojar.sourceforge.net/).

