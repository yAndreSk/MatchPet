package com.matchpet.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.dao.DataAccessException;

import com.matchpet.model.Pet;
import com.matchpet.service.PetService;

@Component
@ConditionalOnProperty(name = "matchpet.cli.enabled", havingValue = "true", matchIfMissing = true)
public class MatchPetCli implements CommandLineRunner {

    private final PetService service;
    private final Scanner scanner;
    private final PrintStream out;

    @Autowired
    public MatchPetCli(PetService service) {
        this(service, System.in, System.out);
    }

    MatchPetCli(PetService service, InputStream input, PrintStream out) {
        this.service = service;
        this.scanner = new Scanner(input);
        this.out = out;
    }

    @Override
    public void run(String... args) {
        out.println("\n=== MatchPet ===");
        boolean executando = true;
        while (executando) {
            imprimirMenu();
            try {
                executando = executar(ler("Escolha uma opção: "));
            } catch (NoSuchElementException exception) {
                out.println("\nEntrada encerrada. A operação em andamento foi cancelada.");
                executando = false;
            } catch (DataAccessException exception) {
                out.println("Não foi possível acessar o MongoDB. Verifique se o servidor está ativo e se MONGODB_URI está correta.");
            } catch (IllegalArgumentException exception) {
                out.println("Erro: " + exception.getMessage());
            }
        }
        out.println("Até logo!");
    }

    private boolean executar(String opcao) {
        switch (opcao) {
            case "1" -> listar();
            case "2" -> buscar();
            case "3" -> cadastrar();
            case "4" -> atualizar();
            case "5" -> excluir();
            case "0" -> {
                return false;
            }
            default -> out.println("Opção inválida.");
        }
        return true;
    }

    private void imprimirMenu() {
        out.println("""

                1 - Listar pets
                2 - Buscar pet
                3 - Cadastrar pet
                4 - Atualizar pet
                5 - Excluir pet
                0 - Sair
                """);
    }

    private void listar() {
        List<Pet> pets = service.listar();
        if (pets.isEmpty()) {
            out.println("Nenhum pet cadastrado.");
            return;
        }
        out.println("ID | Nome | Espécie | Idade");
        pets.forEach(this::imprimir);
        out.println("Total: " + pets.size() + " pet(s).");
    }

    private void buscar() {
        imprimir(service.buscar(ler("ID do pet: ")));
    }

    private void cadastrar() {
        Pet pet = service.cadastrar(
                ler("Nome: "),
                ler("Espécie: "),
                lerIdade());
        out.println("Pet cadastrado com ID: " + pet.getId());
    }

    private void atualizar() {
        String id = ler("ID do pet: ");
        Pet atual = service.buscar(id);
        imprimir(atual);
        out.println("Pressione Enter para manter o valor atual.");
        String nome = ler("Nome [" + atual.getNome() + "]: ");
        String especie = ler("Espécie [" + atual.getEspecie() + "]: ");
        Pet pet = service.atualizar(
                id,
                nome.isBlank() ? atual.getNome() : nome,
                especie.isBlank() ? atual.getEspecie() : especie,
                lerIdade(atual.getIdade()));
        out.println("Pet atualizado: " + pet.getNome());
    }

    private void excluir() {
        String id = ler("ID do pet: ");
        imprimir(service.buscar(id));
        if (!ler("Confirmar exclusão? (s/N): ").equalsIgnoreCase("s")) {
            out.println("Exclusão cancelada.");
            return;
        }
        service.excluir(id);
        out.println("Pet excluído.");
    }

    private void imprimir(Pet pet) {
        out.printf("%s | %s | %s | %d ano(s)%n",
                pet.getId(), pet.getNome(), pet.getEspecie(), pet.getIdade());
    }

    private String ler(String mensagem) {
        out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private int lerIdade() {
        return lerIdade(null);
    }

    private int lerIdade(Integer atual) {
        while (true) {
            String valor = ler(atual == null ? "Idade em anos: " : "Idade [" + atual + "]: ");
            if (valor.isBlank() && atual != null) {
                return atual;
            }
            try {
                int idade = Integer.parseInt(valor);
                if (idade >= 0) {
                    return idade;
                }
            } catch (NumberFormatException exception) {
                // Solicita novamente sem perder os outros campos digitados.
            }
            out.println("Informe uma idade inteira maior ou igual a zero.");
        }
    }
}
