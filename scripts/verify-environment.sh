#!/bin/bash

echo "🔍 Verificação do Ambiente ToneForge"
echo "====================================="
echo ""

echo "📦 Java:"
java -version 2>&1 | head -3
echo ""

echo "🔧 JAVA_HOME:"
echo "JAVA_HOME=$JAVA_HOME"
echo ""

echo "🛠️ Java Compiler:"
javac -version 2>&1
echo ""

echo "🏗️ Gradle:"
./gradlew --version 2>&1 | head -5
echo ""

echo "✅ Ambiente configurado!"
