@echo off
REM Script para remover arquivos DTOs duplicados da pasta raiz dto/
REM Estes arquivos agora existem nas pastas request/ e response/ com pacotes corretos

echo.
echo 🧹 Iniciando limpeza de arquivos DTOs duplicados...
echo.

setlocal enabledelayedexpansion

REM Arquivos a remover
set "FILES[0]=src\main\java\com\encurtador_url\SuperApp\dto\ShortenUrlRequest.java"
set "FILES[1]=src\main\java\com\encurtador_url\SuperApp\dto\ShortenUrlResponse.java"
set "FILES[2]=src\main\java\com\encurtador_url\SuperApp\dto\DetailsUrlResponse.java"
set "FILES[3]=src\main\java\com\encurtador_url\SuperApp\dto\ErrorResponse.java"

REM Remover cada arquivo
for /l %%i in (0,1,3) do (
    if exist "!FILES[%%i]!" (
        echo ❌ Removendo: !FILES[%%i]!
        del /f /q "!FILES[%%i]!"
        echo    ✅ Removido com sucesso
    ) else (
        echo ⚠️  Arquivo não encontrado: !FILES[%%i]!
    )
    echo.
)

echo ✅ Limpeza concluída!
echo.
echo 📁 Estrutura final de DTOs:
echo    dto/
echo    ├── request/
echo    │   └── ShortenUrlRequest.java ✅
echo    └── response/
echo        ├── ShortenUrlResponse.java ✅
echo        ├── DetailsUrlResponse.java ✅
echo        └── ErrorResponse.java ✅
echo.
echo 🎯 Próximo passo: mvn clean compile
echo.
pause

