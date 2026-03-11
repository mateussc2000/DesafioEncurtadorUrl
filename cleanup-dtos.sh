#!/bin/bash

# Script para remover arquivos DTOs duplicados da pasta raiz dto/
# Estes arquivos agora existem nas pastas request/ e response/ com pacotes corretos

echo "🧹 Iniciando limpeza de arquivos DTOs duplicados..."
echo ""

# Arquivos a remover
FILES_TO_REMOVE=(
    "src/main/java/com/encurtador_url/SuperApp/dto/ShortenUrlRequest.java"
    "src/main/java/com/encurtador_url/SuperApp/dto/ShortenUrlResponse.java"
    "src/main/java/com/encurtador_url/SuperApp/dto/DetailsUrlResponse.java"
    "src/main/java/com/encurtador_url/SuperApp/dto/ErrorResponse.java"
)

# Remover cada arquivo
for file in "${FILES_TO_REMOVE[@]}"; do
    if [ -f "$file" ]; then
        echo "❌ Removendo: $file"
        rm -f "$file"
        echo "   ✅ Removido com sucesso"
    else
        echo "⚠️  Arquivo não encontrado: $file"
    fi
    echo ""
done

echo "✅ Limpeza concluída!"
echo ""
echo "📁 Estrutura final de DTOs:"
echo "   dto/"
echo "   ├── request/"
echo "   │   └── ShortenUrlRequest.java ✅"
echo "   └── response/"
echo "       ├── ShortenUrlResponse.java ✅"
echo "       ├── DetailsUrlResponse.java ✅"
echo "       └── ErrorResponse.java ✅"
echo ""
echo "🎯 Próximo passo: mvn clean compile"

