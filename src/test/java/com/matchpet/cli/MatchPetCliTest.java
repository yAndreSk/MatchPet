package com.matchpet.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.matchpet.model.Pet;
import com.matchpet.service.PetService;

@ExtendWith(MockitoExtension.class)
class MatchPetCliTest {

    @Mock
    private PetService service;

    @Test
    void deveExecutarCrudPelaCli() {
        Pet luna = new Pet("1", "Luna", "Cachorro", 3);
        Pet mia = new Pet("1", "Mia", "Gato", 4);
        when(service.listar()).thenReturn(List.of(luna));
        when(service.buscar("1")).thenReturn(luna);
        when(service.cadastrar("Luna", "Cachorro", 3)).thenReturn(luna);
        when(service.atualizar("1", "Mia", "Gato", 4)).thenReturn(mia);

        String entrada = String.join("\n",
                "1",
                "2", "1",
                "3", "Luna", "Cachorro", "3",
                "4", "1", "Mia", "Gato", "4",
                "5", "1", "s",
                "9",
                "0") + "\n";
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        MatchPetCli cli = new MatchPetCli(service,
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(saida, true, StandardCharsets.UTF_8));

        cli.run();

        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Luna | Cachorro | 3 ano(s)"));
        assertTrue(texto.contains("Pet cadastrado com ID: 1"));
        assertTrue(texto.contains("Pet atualizado: Mia"));
        assertTrue(texto.contains("Pet excluído."));
        assertTrue(texto.contains("Opção inválida."));
        assertTrue(texto.contains("Até logo!"));
        verify(service).excluir("1");
    }

    @Test
    void deveExibirListaVaziaEErro() {
        when(service.listar()).thenReturn(List.of());
        when(service.buscar("999")).thenThrow(new IllegalArgumentException("Pet não encontrado: 999"));
        String entrada = String.join("\n", "1", "2", "999", "0") + "\n";
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        MatchPetCli cli = new MatchPetCli(service,
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(saida, true, StandardCharsets.UTF_8));

        cli.run();

        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Nenhum pet cadastrado."));
        assertTrue(texto.contains("Erro: Pet não encontrado: 999"));
    }

    @Test
    void deveSolicitarIdadeNovamenteSemPerderDados() {
        when(service.cadastrar("Luna", "Gato", 0)).thenReturn(new Pet("1", "Luna", "Gato", 0));

        String texto = executar("3\nLuna\nGato\nabc\n-1\n0\n0\n");

        assertTrue(texto.contains("Informe uma idade inteira maior ou igual a zero."));
        verify(service).cadastrar("Luna", "Gato", 0);
    }

    @Test
    void deveManterCamposVaziosNaEdicao() {
        Pet pet = new Pet("1", "Luna", "Gato", 3);
        when(service.buscar("1")).thenReturn(pet);
        when(service.atualizar("1", "Luna", "Gato", 3)).thenReturn(pet);

        executar("4\n1\n\n\n\n0\n");

        verify(service).atualizar("1", "Luna", "Gato", 3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "n", "sim", "qualquer"})
    void deveCancelarExclusaoSemConfirmacaoExplicita(String resposta) {
        when(service.buscar("1")).thenReturn(new Pet("1", "Luna", "Gato", 3));

        assertTrue(executar("5\n1\n" + resposta + "\n0\n").contains("Exclusão cancelada."));

        verify(service, never()).excluir(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "3\n", "3\nLuna\n", "3\nLuna\nGato\n"})
    void deveEncerrarEntradaSemCadastrarDadosParciais(String entrada) {
        assertTrue(executar(entrada).contains("Até logo!"));
        verifyNoInteractions(service);
    }

    @Test
    void deveCancelarEdicaoSeEntradaTerminar() {
        when(service.buscar("1")).thenReturn(new Pet("1", "Luna", "Gato", 3));

        executar("4\n1\nNova Luna\n");

        verify(service, never()).atualizar(any(), any(), any(), anyInt());
    }

    @Test
    void deveOrientarQuandoBancoEstiverIndisponivel() {
        when(service.listar()).thenThrow(new DataAccessResourceFailureException("detalhes internos"));

        assertTrue(executar("1\n0\n").contains("Não foi possível acessar o MongoDB."));
    }

    private String executar(String entrada) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        new MatchPetCli(service,
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(saida, true, StandardCharsets.UTF_8)).run();
        return saida.toString(StandardCharsets.UTF_8);
    }
}
