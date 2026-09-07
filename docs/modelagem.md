# Modelagem

O banco possui uma única coleção chamada `pets`.

Todos os documentos têm a mesma estrutura simples:

```json
{
  "_id": "67c123...",
  "nome": "Luna",
  "especie": "Cachorro",
  "idade": 3
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `_id` | ObjectId | identificador gerado ao salvar, exibido como texto na CLI |
| `nome` | texto | nome do pet |
| `especie` | texto | espécie do pet |
| `idade` | número inteiro | idade em anos |

Não há outras coleções, relacionamentos, listas ou objetos aninhados.

O Spring Data acrescenta o campo técnico de texto `_class` com o nome da classe Pet. Todos os pets são gravados com o mesmo formato.
