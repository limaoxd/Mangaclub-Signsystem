#!/bin/bash
kotlinc Main.kt -include-runtime -d Main.jar
java -jar Main.jar

echo 'Kotlin files are setted up.'