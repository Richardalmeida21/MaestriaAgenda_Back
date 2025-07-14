# Solução para Problemas de Conexão com Supabase no Render

Este documento contém as etapas para resolver problemas de conexão entre uma aplicação Spring Boot hospedada no Render e um banco de dados PostgreSQL no Supabase.

## Problema Identificado

O principal problema identificado foi a falha de resolução DNS para o host do Supabase (`db.kgcajiuuvcgkggbhtudi.supabase.co`) no ambiente Render. Isso resulta no erro:

```
java.net.UnknownHostException: db.kgcajiuuvcgkggbhtudi.supabase.co
```

Este erro impede que a aplicação se conecte ao banco de dados PostgreSQL no Supabase.

## Soluções Implementadas

1. **Detecção de Ambiente**: Implementamos configurações específicas para o ambiente Render.

2. **Configuração do Hikari Pool**: Ajustamos o pool de conexões Hikari para lidar melhor com problemas de conectividade.

3. **Diagnóstico de DNS**: Adicionamos código para verificar e diagnosticar problemas de DNS.

4. **Dockerfile Otimizado**: Criamos um Dockerfile otimizado com ferramentas de diagnóstico de rede e um script de inicialização para tentar resolver problemas de DNS.

5. **Servidores DNS Alternativos**: Configuramos o contêiner Docker para usar servidores DNS alternativos (Google 8.8.8.8 e Cloudflare 1.1.1.1).

## Usando a Aplicação

### Ambiente Local

No ambiente local, a aplicação deve funcionar normalmente, conectando-se diretamente ao banco de dados Supabase.

### Ambiente Render

No ambiente Render, a aplicação:

1. Inicia com o perfil `render` ativado
2. Executa verificações de DNS e conectividade
3. Configura servidores DNS alternativos
4. Tenta conectar-se ao banco de dados com configurações otimizadas

## Resolução de Problemas

Se a aplicação ainda estiver enfrentando problemas de conexão no Render:

1. **Verifique os Logs do Render**: Procure por erros de DNS ou conectividade nos logs.

2. **Verifique a Configuração do Supabase**: Confirme se o host do Supabase está correto e se o banco de dados está configurado para aceitar conexões externas.

3. **Variáveis de Ambiente**: Certifique-se de que as variáveis de ambiente no Render estão configuradas corretamente.

4. **Tente IPs Diretamente**: Se a resolução DNS continuar sendo um problema, você pode tentar usar o endereço IP direto em vez do nome de host na URL de conexão do banco de dados.

5. **Crie uma Instância de Banco de Dados no Render**: Como alternativa, considere criar uma instância de banco de dados PostgreSQL no próprio Render para evitar problemas de comunicação entre serviços.

## Suporte

Se você continuar enfrentando problemas, consulte:

- [Documentação do Render sobre problemas de rede](https://docs.render.com/troubleshooting-network)
- [Documentação do Supabase sobre conexões externas](https://supabase.com/docs/guides/database/connecting-to-postgres)
