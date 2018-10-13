## What is this?
This plugin rolls up your current project's jar, resources, and all of its
dependencies into a self-contained archive with only the minimal set of class
and resource files necessary to the project. It collects all the parameters
based on reasonable defaults from the project and forks off a process to run
`Autojar` with these collected values.

## What is Autojar?
Here's a description of what `Autojar` does from the project page:
```
Autojar creates jar archives of minimal size from different sources (classes,
directories, libraries). Starting from one or more classes, it scans the
bytecode recursively for other classes, extracts them from their archives if
necessary, and adds them to the output file. The resulting archive contains only
classes that are really needed. Thus the size and loading time of applets can be
kept low, and applications can be made self-contained.
```
You can read more about `Autojar` [here](http://autojar.sourceforge.net/).

## Examples

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
        classpath 'com.github.rholder:gradle-autojar:1.0.1'
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
    autojarFiles = ['foo.txt', 'dir/foo2.txt']

    // additional classes can be manually added when they're not included automatically
    autojarClasses = ['org.apache.commons.lang.time.DateUtils']

    // override the defaults of -ba
    autojarExtra = '-bav'
}
```

## You probably don't need this
This plugin is designed to output a minimized executable artifact. It is
extraordinarily bad to use on a large, complicated project with only minimal
or ad-hoc test coverage of the final deliverable. I wouldn't recommend using
this for anything that resembles a regular deployment where development is
ongoing or release cycles that are dependent on other upstream pieces of
software that are outside of the original developers' control are involved.
The consequences of missing a critical class that's only loaded during runtime
usually isn't worth the tradeoff of a smaller final artifact size. There are
better ways of rolling up a single artifact that can guarantee that all runtime
dependencies stay intact (see [FatJAR](https://github.com/musketyr/gradle-fatjar-plugin)
and [One-JAR](https://github.com/rholder/gradle-one-jar)).

## Fine, when SHOULD I use this?
If you have a standalone utility that depends on only a limited subset of large
and unwieldy libraries (like [fastutil](http://fastutil.di.unimi.it/) or the
[AWS SDK for Java](http://aws.amazon.com/sdkforjava/)) then this might be a
reasonable artifact packaging solution.

## License
The `gradle-autojar` build plugin is released under version 2.0 of the [Apache
License](http://www.apache.org/licenses/LICENSE-2.0). This plugin comes bundled
with an unmodified release of `Autojar`, which is licensed under the terms set
forth by the GPLv2. In compliance with the use of GPLv2 licensed software,
`gradle-autojar` only utilizes a fork and exec mechanism for running an
unmodified `Autojar` process whereby derived arguments are passed from the
plugin. The original source code for `Autojar` can be found [here](http://autojar.sourceforge.net/).

