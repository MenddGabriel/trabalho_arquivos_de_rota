package com.trabalho.ed2;

public class Main {
    public static void main(String[] args) {
        GerenciadorArquivos gerenciador = new GerenciadorArquivos();

        try {
            // 1. Testa a leitura do config e criação das pastas
            gerenciador.inicializarConfiguracao();
            System.out.println("Configuração carregada e pastas criadas com sucesso!");

            // 2. Testa o processamento dos arquivos.
            // Se o arquivo não existir, ele apenas avisa no console.
            gerenciador.processarTodosArquivos();

        } catch (Exception e) {
            System.out.println("Erro na inicialização: " + e.getMessage());
        }
    }
}