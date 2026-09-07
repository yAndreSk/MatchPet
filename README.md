# MatchPet

PoC simples em Java e MongoDB para demonstrar operações CRUD pela linha de comando.

## Escopo

O sistema trabalha com:

- uma única coleção NoSQL: `pets`;
- documentos homogêneos e sem objetos aninhados;
- cadastro, listagem, busca, atualização e exclusão;
- uma interface de linha de comando, sem frontend.

Cada pet possui os campos de negócio `nome`, `especie` e `idade`, além do identificador:

```json
{
  "_id": "67c123...",
  "nome": "Luna",
  "especie": "Cachorro",
  "idade": 3
}
```

O Spring Data também grava o campo técnico de texto `_class`. Os documentos continuam simples e homogêneos.

## Executar

É necessário ter:

- JDK 21;
- MongoDB Community Server instalado e em execução.

Por padrão, o sistema usa:

```text
mongodb://localhost:27017/matchpet
```

No Windows, abra o terminal na pasta extraída do projeto, onde estão `pom.xml` e `mvnw.cmd`:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Para usar outra conexão, defina a variável `MONGODB_URI` antes de iniciar:

```powershell
$env:MONGODB_URI = "mongodb://localhost:27017/matchpet"
.\mvnw.cmd spring-boot:run
```

No MongoDB Compass, conecte-se a `mongodb://localhost:27017/matchpet` e abra a coleção `pets`.

O Compass é um cliente: o servidor MongoDB precisa estar em execução. O MatchPet não inicia nem encerra o servidor. Ao sair da CLI, seus dados permanecem no banco.

## Menu

```text
1 - Listar pets
2 - Buscar pet
3 - Cadastrar pet
4 - Atualizar pet
5 - Excluir pet
0 - Sair
```

A idade deve ser um inteiro maior ou igual a zero. Ao editar, pressione Enter para manter um campo. A exclusão exige confirmação com `s`.

O banco começa vazio; cadastre seus pets pelo menu. Nenhum exemplo é recriado automaticamente depois de uma exclusão.

## Demonstração da primeira entrega

1. Cadastre Luna, espécie Cachorro, idade 3, pela opção 3.
2. Liste os pets na opção 1 e copie o ID exibido.
3. Busque esse ID na opção 2.
4. Na opção 4, altere a idade para 4, mantendo os demais campos com Enter.
5. Saia, abra novamente o programa e confira que a alteração foi persistida.
6. Exclua o pet pela opção 5 e confirme com `s`. Confira a listagem vazia.

As operações usam somente a coleção `pets`. Não são necessários cadastros de pessoas, abrigos ou solicitações para esta entrega.

## Estrutura

```text
CLI -> PetService -> PetRepository -> coleção pets
```

O código usa apenas um modelo, um repositório e um serviço. Não há relacionamentos, subdocumentos, DTOs ou mappers.

## Testes

```powershell
.\mvnw.cmd clean verify
```

Os testes unitários verificam o serviço e a CLI sem depender do banco em execução.
