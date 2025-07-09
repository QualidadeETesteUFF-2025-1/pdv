#!/bin/bash

echo "=== PDV - Análise de Cobertura de Testes ==="
echo ""

# Verificar se Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven não está instalado ou não está no PATH"
    exit 1
fi

echo "✅ Maven encontrado: $(mvn -version | head -1)"
echo ""

# Executar testes com JaCoCo
echo "📊 Executando testes com cobertura JaCoCo..."
mvn clean test jacoco:report

if [ $? -eq 0 ]; then
    echo "✅ Testes executados com sucesso"
    echo "📋 Relatório JaCoCo: target/site/jacoco/index.html"
else
    echo "❌ Falha nos testes"
    exit 1
fi

echo ""

# Executar testes de mutação
echo "🧬 Executando testes de mutação com PIT..."
mvn org.pitest:pitest-maven:mutationCoverage

if [ $? -eq 0 ]; then
    echo "✅ Testes de mutação executados com sucesso"
    echo "📋 Relatório PIT: target/pit-reports/*/index.html"
else
    echo "❌ Falha nos testes de mutação"
    exit 1
fi

echo ""
echo "=== Relatórios Gerados ==="
echo "🔍 JaCoCo (Cobertura de Arestas): target/site/jacoco/index.html"
echo "🧬 PIT (Testes de Mutação): target/pit-reports/*/index.html"
echo ""
echo "✅ Análise de cobertura concluída com sucesso!" 