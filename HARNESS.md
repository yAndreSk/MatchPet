# Validação

Requisitos:

- JDK 21;
- MongoDB disponível em `localhost:27017`.

## Testar

```powershell
.\mvnw.cmd clean verify
```

## Executar

```powershell
.\mvnw.cmd spring-boot:run
```

A conexão padrão é `mongodb://localhost:27017/matchpet`. Para alterá-la, use a variável de ambiente `MONGODB_URI`.

## Gerar o JAR

```powershell
.\mvnw.cmd clean package
java -jar target\matchpet-1.0.0.jar
```

Verifique pelo menu as operações de listar, buscar, cadastrar, atualizar e excluir. No MongoDB Compass, confirme os dados no banco `matchpet`, coleção `pets`.
