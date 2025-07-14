# Instruções para configuração do Render

Para configurar as variáveis de ambiente no Render, siga estes passos:

1. Acesse o painel do Render e vá para o seu serviço web
2. Vá em "Environment" na barra lateral
3. Configure as seguintes variáveis:

## Variáveis para o Supabase

JDBC_DATABASE_URL=jdbc:postgresql://db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres
JDBC_DATABASE_USERNAME=postgres
JDBC_DATABASE_PASSWORD=[SUA-SENHA-AQUI]

## IMPORTANTE: Formato da URL

Se você tiver a URL no formato:
   postgresql://postgres:senha@db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres

Deve convertê-la para:
   jdbc:postgresql://db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres
   
E definir usuário e senha separadamente nas variáveis acima.

## Solução de problemas comuns

1. Certifique-se de que seu projeto Supabase permite conexões do IP do Render
   - Vá para Project Settings > Database > Connection Pooling no Supabase
   - Verifique a configuração de "Connection string"
   - Se necessário, adicione o IP do Render na lista de IPs permitidos

2. Se continuar tendo problemas, tente:
   - Adicionar "?sslmode=require" ao final da URL
   - Verificar se o banco postgres realmente existe no Supabase
   - Verificar logs de erro no Render para mensagens mais detalhadas
