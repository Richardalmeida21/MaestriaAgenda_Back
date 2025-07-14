# Soluções para Problemas JPA/Hibernate com Supabase

## Problema Específico:
```
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'entityManagerFactory'
org.hibernate.exception.JDBCConnectionException: Unable to open JDBC Connection for DDL execution [The connection attempt failed.]
```

Este erro indica que o Hibernate não conseguiu estabelecer uma conexão com o banco de dados durante a inicialização da aplicação, o que impede a criação do `EntityManagerFactory`.

## Configurações atualizadas no `application.properties`:

1. **Configurações Hikari otimizadas:**
   ```properties
   spring.datasource.hikari.maximumPoolSize=1
   spring.datasource.hikari.minimumIdle=1
   spring.datasource.hikari.connectionTimeout=20000
   spring.datasource.hikari.leakDetectionThreshold=60000
   spring.datasource.hikari.connectionTestQuery=SELECT 1
   spring.datasource.hikari.validationTimeout=5000
   spring.datasource.hikari.initializationFailTimeout=10000
   ```

2. **Logs de depuração aprimorados:**
   ```properties
   logging.level.org.hibernate=ERROR
   logging.level.com.zaxxer.hikari=DEBUG
   logging.level.org.postgresql=DEBUG
   logging.level.org.springframework.jdbc=DEBUG
   logging.level.org.springframework.orm.jpa=DEBUG
   logging.level.org.springframework.transaction=DEBUG
   ```

## Verificação das variáveis de ambiente no Render:

As variáveis estão corretas no Render:
- JDBC_DATABASE_URL: `jdbc:postgresql://db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres?sslmode=require`
- JDBC_DATABASE_USERNAME: `postgres`
- JDBC_DATABASE_PASSWORD: `IIrr2931!`

## Verificação das configurações do Supabase:

Na interface atual do Supabase, você pode verificar e ajustar as configurações de conexão:

1. **Connection Pooling**:
   - No dashboard do Supabase, clique em "Connect" na barra superior
   - Verifique a seção "Connection pooling configuration"
   - O Pool Size padrão é 15 (para o plano Nano)
   - O Max Client Connections é 200 (fixo para o plano Nano)
   - Estas configurações são importantes para entender os limites de conexão

2. **SSL Configuration**:
   - Verifique se "Enforce SSL on incoming connections" está habilitado
   - Se estiver, sua URL de conexão DEVE incluir `sslmode=require`

3. **Network Restrictions**:
   - Por padrão, o Supabase permite conexões de qualquer IP
   - Se houver restrições configuradas, o IP do Render precisa estar na lista de permissões

## Soluções adicionais para tentar:

### 1. Implementar um mecanismo de retry para inicialização

Crie uma classe chamada `DatabaseConnectionInitializer.java` em `com.maestria.agenda.config`:

```java
package com.maestria.agenda.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseConnectionInitializer {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionInitializer.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @PostConstruct
    public void initializeDatabase() {
        int maxRetries = 5;
        int retryCount = 0;
        boolean connected = false;
        
        while (!connected && retryCount < maxRetries) {
            try {
                // Tente fazer uma consulta simples
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                connected = true;
                log.info("Conexão com o banco de dados estabelecida com sucesso");
            } catch (Exception e) {
                retryCount++;
                log.error("Falha na conexão com o banco de dados, tentativa {} de {}. Erro: {}", 
                          retryCount, maxRetries, e.getMessage());
                try {
                    // Aguarde antes de tentar novamente
                    Thread.sleep(5000); // 5 segundos
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        if (!connected) {
            log.error("Não foi possível estabelecer conexão com o banco de dados após {} tentativas", maxRetries);
        }
    }
}
```

### 2. Verificar as configurações de rede no Supabase

De acordo com a interface atualizada do Supabase:

1. Acesse o painel do Supabase e selecione seu projeto
2. Clique no botão "Connect" na barra superior
3. Na seção "Network Restrictions":
   - Clique em "Add restriction" para adicionar o IP do Render
   - Se não conseguir encontrar esta opção, procure por "IP Allow List" ou "Network Access"
   - Por padrão, o Supabase permite acesso de todos os IPs, mas é importante verificar se não há restrições configuradas

Também verifique a seção "SSL Configuration":
   - Certifique-se de que "Enforce SSL on incoming connections" está habilitado
   - O parâmetro `sslmode=require` na URL de conexão deve estar alinhado com esta configuração

### 3. Considerar usar Retry no nível de aplicação com Spring Retry

Adicione as seguintes dependências no `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
</dependency>
```

E ative o Spring Retry na sua classe principal:

```java
@SpringBootApplication
@EnableRetry
public class AgendaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendaApplication.class, args);
    }
}
```

### 4. Verifique os logs detalhados no Render e obtenha o IP do serviço

Os logs do Render podem fornecer informações mais detalhadas sobre o erro de conexão.

Para descobrir o IP do seu serviço no Render:
1. Acesse o dashboard do Render e selecione seu serviço
2. Vá para a aba "Logs"
3. Execute um comando de diagnóstico de rede, adicionando temporariamente o seguinte ao seu projeto:
   ```java
   @GetMapping("/ip-check")
   public String checkIp() throws Exception {
       URL url = new URL("https://checkip.amazonaws.com");
       BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
       return br.readLine().trim();
   }
   ```
4. Acesse este endpoint e o IP externo será mostrado nos logs
5. Use este IP para adicionar às Network Restrictions do Supabase

### 5. Teste a conexão com ferramentas externas

Para validar se é um problema específico do Spring Boot ou da conexão em geral, teste a conexão usando ferramentas como:

1. **psql** - cliente PostgreSQL na linha de comando:
   ```bash
   psql "postgres://postgres:IIrr2931!@db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres?sslmode=require"
   ```

2. **DBeaver ou outro cliente SQL**:
   - Host: db.kgcajiuuvcgkggbhtudi.supabase.co
   - Port: 5432
   - Database: postgres
   - Username: postgres
   - Password: IIrr2931!
   - SSL: required

3. **Teste simples com Java puro**:
   ```java
   import java.sql.Connection;
   import java.sql.DriverManager;
   import java.util.Properties;

   public class TestDatabaseConnection {
       public static void main(String[] args) {
           String url = "jdbc:postgresql://db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres?sslmode=require";
           Properties props = new Properties();
           props.setProperty("user", "postgres");
           props.setProperty("password", "IIrr2931!");
           props.setProperty("ssl", "true");
           
           try {
               Connection conn = DriverManager.getConnection(url, props);
               System.out.println("Conexão estabelecida com sucesso!");
               conn.close();
           } catch (Exception e) {
               System.err.println("Erro ao conectar: " + e.getMessage());
               e.printStackTrace();
           }
       }
   }
   ```

### 6. Possível problema com conexões SSL e configuração específica

Se o problema persistir com conexões SSL, você pode tentar as seguintes configurações adicionais no `application.properties`:

```properties
# Configurações adicionais para SSL com Supabase
spring.datasource.hikari.data-source-properties.ssl=true
spring.datasource.hikari.data-source-properties.sslfactory=org.postgresql.ssl.NonValidatingFactory
spring.datasource.hikari.data-source-properties.sslmode=require
```

Ou alternativamente, se for apenas para testes e diagnóstico, desabilitar temporariamente o SSL:

```properties
# Apenas para testes! Remover em produção!
spring.datasource.url=jdbc:postgresql://db.kgcajiuuvcgkggbhtudi.supabase.co:5432/postgres?sslmode=disable
```

### 7. Contato com suporte técnico e recursos adicionais

Se nenhuma das soluções anteriores funcionar:

1. **Contato com suporte**:
   - Suporte do Supabase: https://supabase.com/support
   - Suporte do Render: https://render.com/docs/getting-help
   - Comunidade do Supabase: https://github.com/supabase/supabase/discussions

2. **Documentação específica**:
   - [Supabase Connection Pooling](https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooling)
   - [Configuração SSL no PostgreSQL](https://www.postgresql.org/docs/current/libpq-ssl.html)
   - [HikariCP - Configurações avançadas](https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby)

3. **Alternativas a considerar**:
   - Se o problema persistir, considere usar outro serviço de banco de dados PostgreSQL como Railway, ElephantSQL ou um banco gerenciado pelo próprio Render
   - Para testes, configure um banco PostgreSQL local e confirme que a aplicação funciona corretamente
