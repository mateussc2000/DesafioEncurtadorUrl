# 🐳 Docker Setup - Encurtador de URLs

## 📋 Pré-requisitos

- Docker instalado e rodando
- Docker Compose instalado

## 🚀 Como executar

### 1. Construir e executar com Docker Compose

```bash
# Construir a imagem e iniciar o container
docker-compose up --build

# Ou em background
docker-compose up -d --build
```

### 2. Acessar a aplicação

- **Aplicação**: http://localhost:8080
- **Console H2**: http://localhost:8080/h2-console
  - **JDBC URL**: `jdbc:h2:file:./data/shortenerdb`
  - **Username**: `sa`
  - **Password**: (vazio)

### 3. Parar a aplicação

```bash
# Parar containers
docker-compose down

# Parar e remover volumes
docker-compose down -v
```

## 📁 Estrutura dos arquivos

```
.
├── Dockerfile              # Configuração do container da aplicação
├── compose.yaml           # Configuração do Docker Compose
├── .dockerignore          # Arquivos ignorados no build
└── data/                  # Volume montado para persistir dados H2
```

## 🔧 Comandos úteis

```bash
# Ver logs da aplicação
docker-compose logs -f app

# Acessar shell do container
docker-compose exec app sh

# Ver status dos containers
docker-compose ps

# Limpar imagens não utilizadas
docker system prune -f
```

## 📊 Arquitetura

```
┌─────────────────────────────────────┐
│         Docker Container            │
├─────────────────────────────────────┤
│  Spring Boot App (porta 8080)       │
│  + H2 Database (arquivo local)      │
│  + Volume montado: ./data           │
└─────────────────────────────────────┘
```

## ⚠️ Observações

- Os dados do H2 são persistidos no diretório `./data` do host
- A aplicação roda em modo desenvolvimento
- Para produção, considere usar PostgreSQL em container separado
