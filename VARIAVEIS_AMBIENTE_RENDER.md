# Variáveis de Ambiente para o Render

Este arquivo lista todas as variáveis de ambiente necessárias para configurar a aplicação no Render.

## Variáveis Obrigatórias

| Nome da Variável | Descrição | Exemplo |
|------------------|-----------|---------|
| `JDBC_DATABASE_URL` | URL de conexão com o banco de dados Supabase | `jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:6543/postgres?user=postgres.kgcajiuuvcgkggbhtudi&password=suasenha&sslmode=require` |
| `JDBC_DATABASE_USERNAME` | Nome de usuário do banco de dados | `postgres.kgcajiuuvcgkggbhtudi` |
| `JDBC_DATABASE_PASSWORD` | Senha do banco de dados | `suasenha` |
| `JWT_SECRET` | Chave secreta para geração de tokens JWT | `Y3i49Jx8nQw3sP@74LkF9dC4mJ1N2PZz` |
| `SPRING_PROFILES_ACTIVE` | Perfil ativo do Spring | `render` |

## Variáveis Opcionais

| Nome da Variável | Descrição | Valor Padrão |
|------------------|-----------|-------------|
| `COMISSAO_PERCENTUAL` | Percentual de comissão | `70.0` |
| `WHATSAPP_API_TOKEN` | Token da API do WhatsApp | - |
| `WHATSAPP_PHONE_NUMBER_ID` | ID do número de telefone do WhatsApp | - |
| `WHATSAPP_API_VERSION` | Versão da API do WhatsApp | - |
| `WHATSAPP_ENABLED` | Habilitar integração com WhatsApp | `true` |
| `WHATSAPP_WEBHOOK_VERIFICATION_TOKEN` | Token de verificação do webhook do WhatsApp | - |

## Como Configurar no Render

1. Acesse o Dashboard do Render
2. Vá para o seu serviço web
3. Clique na aba "Environment"
4. Adicione cada variável de ambiente necessária
5. Clique em "Save Changes"
6. Reinicie o serviço para aplicar as alterações

## Notas Importantes

- Não inclua espaços ou aspas nas variáveis de ambiente
- Valores com caracteres especiais podem precisar ser escapados
- Certifique-se de que a URL do banco de dados está correta e inclui todos os parâmetros necessários
