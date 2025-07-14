# Configuração Simplificada para o Render

Este documento explica a configuração simplificada para executar a aplicação no Render, sem configurações complexas de DNS.

## Alterações Realizadas

1. **Configuração do Spring Boot**:
   - Removemos a exclusão do `DataSourceAutoConfiguration` no `AgendaApplication.java`
   - Aplicação agora está configurada para usar normalmente o banco de dados

2. **Uso de Variáveis de Ambiente**:
   - A conexão com o banco de dados usa variáveis de ambiente:
     ```
     spring.datasource.url=${JDBC_DATABASE_URL}
     spring.datasource.username=${JDBC_DATABASE_USERNAME}
     spring.datasource.password=${JDBC_DATABASE_PASSWORD}
     ```
   - Isso permite configurar facilmente a conexão no ambiente Render

3. **Dockerfile Simplificado**:
   - Removemos as configurações complexas de DNS e ferramentas de diagnóstico
   - Usamos um contêiner simples com o perfil `render` ativado

## Como Usar

1. **Ambiente Local**:
   - Configure variáveis de ambiente localmente ou use um arquivo `application-local.properties`

2. **Ambiente Render**:
   - Configure as variáveis de ambiente no Render:
     - `JDBC_DATABASE_URL`: URL completa de conexão com o PostgreSQL
     - `JDBC_DATABASE_USERNAME`: Nome de usuário do banco de dados
     - `JDBC_DATABASE_PASSWORD`: Senha do banco de dados
     - `JWT_SECRET`: Chave secreta para geração de tokens JWT
     - `COMISSAO_PERCENTUAL`: Percentual de comissão (opcional)
     - `SPRING_PROFILES_ACTIVE`: Defina como `render`
     - Variáveis do WhatsApp (se necessário): `WHATSAPP_API_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_API_VERSION`, `WHATSAPP_ENABLED`, `WHATSAPP_WEBHOOK_VERIFICATION_TOKEN`

3. **Implantação**:
   - Use o Dockerfile simplificado para implantar no Render
   - O Render lidará com a compilação e implantação da aplicação

## Solução de Problemas

Se a aplicação não conseguir se conectar ao banco de dados no Render:

1. **Verifique as Variáveis de Ambiente**: Certifique-se de que as variáveis de ambiente estão configuradas corretamente no painel do Render.

2. **Verifique o Acesso ao Banco de Dados**: Confirme se o banco de dados PostgreSQL no Supabase está configurado para aceitar conexões externas do Render.

3. **Logs do Render**: Verifique os logs do serviço no Render para identificar problemas específicos de conexão.
