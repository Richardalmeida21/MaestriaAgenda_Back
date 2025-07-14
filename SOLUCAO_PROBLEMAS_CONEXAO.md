# Soluções para Problemas de Conexão com Supabase no Render

Se você está enfrentando o erro "Network is unreachable" ou outros problemas de conexão entre o Render e o Supabase, tente as seguintes soluções:

## 1. Verificar configurações no Supabase

1. Acesse o Supabase Dashboard: https://supabase.com/dashboard/project/[seu-projeto]
2. Vá em **Project Settings** > **Database**
3. Verifique a seção **Connection Info**
4. Certifique-se de que a opção **Restricting IPs** não está bloqueando o Render
   - Se necessário, adicione o IP do Render à lista de IPs permitidos
   - Você pode obter o IP do Render nas logs do seu serviço

## 2. Configurações do SSL

O Supabase requer conexões SSL. Adicione o parâmetro `sslmode=require` à URL de conexão.

Já fizemos isso no arquivo `application.properties`:
```
spring.datasource.url=${JDBC_DATABASE_URL}?sslmode=require
```

## 3. Verifique as variáveis de ambiente no Render

1. Verifique se a URL está no formato JDBC correto:
   ```
   jdbc:postgresql://db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres
   ```

2. Verifique se o usuário e senha estão corretos

## 4. Problemas de Proxy no Render

Em alguns casos, o Render pode ter problemas de conexão com serviços externos devido a configurações de rede internas.

Soluções possíveis:
1. Reduza o pool de conexões (já fizemos isso no application.properties)
2. Adicione timeouts mais curtos para a conexão
3. Entre em contato com o suporte do Render mencionando que está tendo problemas para conectar ao Supabase

## 5. Verificar Logs do Render

Os logs podem fornecer mais detalhes sobre o erro de conexão:
1. Acesse seu serviço no Render
2. Clique em "Logs" na barra lateral
3. Procure por erros relacionados a conexão de banco de dados

## 6. Teste com outro provedor

Se continuar tendo problemas, você pode considerar:
1. Usar um banco de dados gerenciado pelo próprio Render
2. Usar outro provedor como Railway ou Heroku
3. Usar o PostgreSQL do Railway, que geralmente tem boa conectividade com o Render

## 7. Contato

Se nenhuma dessas soluções funcionar, entre em contato com:
- Suporte do Supabase: https://supabase.com/support
- Suporte do Render: https://render.com/docs/getting-help
