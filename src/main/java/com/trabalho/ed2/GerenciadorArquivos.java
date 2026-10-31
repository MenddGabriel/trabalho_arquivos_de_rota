package com.trabalho.ed2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class GerenciadorArquivos {

    private String pastaRaiz;
    private String pastaProcessado;
    private String pastaNaoProcessado;

    public GerenciadorArquivos() {
        // Define os caminhos padrão
        this.pastaRaiz = "C:\\Teste";
        this.pastaProcessado = "C:\\Teste\\Processado";
        this.pastaNaoProcessado = "C:\\Teste\\NaoProcessado";
    }

    /**
     * Lê o arquivo config.txt e cria os diretórios.
     */
    public void inicializarConfiguracao() throws Exception {
        File pastaConfig = new File("C:\\Teste\\Configuracao");
        File arquivoConfig = new File(pastaConfig, "config.txt");

        // Teste 1.1: Rodar sem criar os diretórios manual, vai criar automaticamente.
        if (!pastaConfig.exists()) {
            // Tenta criar a pasta (e subpastas, se necessário)
            boolean criada = pastaConfig.mkdirs();

            // Lança erro apenas se o sistema operacional não conseguir criar a pasta
            if (!criada) {
                throw new Exception("EXCEÇÃO: Não foi possível criar o diretório " + pastaConfig.getAbsolutePath());
            }
        }

        // Teste 1.2:  Verificar e criar o arquivo config.txt se não existir
        if (!arquivoConfig.exists()) {
            // Cria o arquivo e escreve o conteúdo padrão
            try (java.io.FileWriter fw = new java.io.FileWriter(arquivoConfig);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {

                bw.write("Processado=C:\\Teste\\Processado");
                bw.newLine(); // Pula para a próxima linha
                bw.write("Nao Processado=C:\\Teste\\NaoProcessado");

                System.out.println("Arquivo config.txt criado com sucesso com as configurações padrão.");
            } catch (java.io.IOException e) {
                throw new Exception("EXCEÇÃO: Falha ao criar o arquivo config.txt padrão.", e);
            }
        }

        List<String> linhas = Files.readAllLines(arquivoConfig.toPath());


        // Teste 1.3: Arquivo em branco ou incompleto
        // Define o conteúdo padrão esperado
        String linha1Esperada = "Processado=C:\\Teste\\Processado";
        String linha2Esperada = "Nao Processado=C:\\Teste\\NaoProcessado";

        // Verifica se está vazio, incompleto OU se o conteúdo de alguma linha está incorreto
        if (linhas.size() < 2 || !linhas.get(0).equals(linha1Esperada) || !linhas.get(1).equals(linha2Esperada)) {
            System.out.println("Arquivo config.txt inválido, incompleto ou com valores incorretos. Corrigindo...");

            // Sobrescreve o arquivo com os dados corretos
            try (java.io.FileWriter fw = new java.io.FileWriter(arquivoConfig, false);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {

                bw.write(linha1Esperada);
                bw.newLine();
                bw.write(linha2Esperada);

                System.out.println("Arquivo config.txt corrigido com sucesso.");

                // Atualiza a lista 'linhas' na memória para o restante do programa rodar certo
                linhas.clear();
                linhas.add(linha1Esperada);
                linhas.add(linha2Esperada);

            } catch (java.io.IOException e) {
                throw new Exception("EXCEÇÃO: O arquivo de configuração estava incorreto e não foi possível corrigi-lo.", e);
            }
        }



        // Lê as linhas do config (Teste 1.5: Substituir '=' por '@' vai cair no else)
        for (String linha : linhas) {
            if (linha.startsWith("Processado=")) {
                this.pastaProcessado = linha.split("=")[1];
            } else if (linha.startsWith("Não Processado=") || linha.startsWith("Nao Processado=")) {
                this.pastaNaoProcessado = linha.split("=")[1];
            } else if (!linha.trim().isEmpty()) {
                throw new Exception("EXCEÇÃO: Formato inválido no config.txt (falta o '=').");
            }
        }

        // Cria os diretórios de destino se não existirem
        new File(pastaProcessado).mkdirs();
        new File(pastaNaoProcessado).mkdirs();
    }

    /**
     * Busca todos os arquivos rotaNN.txt na pasta raiz e os processa.
     * Requisito do PDF: "O sistema deverá permitir a leitura de vários arquivos... e tratar TODOS ao mesmo tempo."
     */
    public void processarTodosArquivos() {
        File diretorio = new File(pastaRaiz);

        // Lista todos os arquivos que começam com "rota" e terminam com ".txt" (ignorando maiúsculas/minúsculas)
        File[] arquivos = diretorio.listFiles((dir, nome) ->
                nome.toLowerCase().startsWith("rota") && nome.toLowerCase().endsWith(".txt")
        );

        if (arquivos == null || arquivos.length == 0) {
            System.out.println("Nenhum arquivo rotaNN.txt encontrado na pasta " + pastaRaiz);
            return;
        }

        System.out.println("Encontrados " + arquivos.length + " arquivos para processar.\n");

        for (File arquivo : arquivos) {
            processarArquivoRota(arquivo.getName());
        }
    }

    /**
     * Processa um único arquivo rotaNN.txt, valida e o move para a pasta correta.
     */
    public void processarArquivoRota(String nomeArquivo) {
        File arquivo = new File(pastaRaiz, nomeArquivo);

        if (!arquivo.exists()) {
            System.out.println("Arquivo não encontrado: " + nomeArquivo);
            return;
        }

        try {
            validarArquivo(arquivo);

            moverArquivo(arquivo, pastaProcessado);
            System.out.println("SUCESSO: " + nomeArquivo + " movido para Processado.");

        } catch (Exception e) {
            System.out.println("ERRO no arquivo " + nomeArquivo + ": " + e.getMessage());
            moverArquivo(arquivo, pastaNaoProcessado);
        }
    }

    /**
     * Realiza as validações de Header e Trailer.
     */
    private void validarArquivo(File arquivo) throws Exception {
        List<String> linhas = Files.readAllLines(arquivo.toPath());

        if (linhas.isEmpty()) {
            throw new Exception("Arquivo vazio.");
        }

        String header = linhas.get(0);
        String trailer = linhas.get(linhas.size() - 1);

        // --- VALIDAÇÃO DO HEADER (Testes 1.6 a 1.8) ---
        if (!header.startsWith("00")) {
            throw new Exception("Header inválido: Deve começar com 00.");
        }

        if (header.length() != 9) {
            throw new Exception("Header inválido: Tamanho incorreto (esperado 9 caracteres). Encontrado: " + header);
        }

        int somaPesosHeader;
        try {
            // Pega os 5 últimos caracteres do header e converte para número
            somaPesosHeader = Integer.parseInt(header.substring(4, 9));
        } catch (NumberFormatException e) {
            throw new Exception("Header inválido: A soma de pesos (SP) não é um número válido.");
        }

        // --- VALIDAÇÃO DO TRAILER ---
        if (!trailer.startsWith("09")) {
            throw new Exception("Trailer inválido: Deve começar com 09.");
        }

        if (trailer.length() != 18) {
            throw new Exception("Trailer inválido: Tamanho incorreto (esperado 18 caracteres). Encontrado: " + trailer.length());
        }

        int numLinhasConexao;
        int numLinhasPesos;
        int somaPesosTrailer;
        try {
            // Formato: 09 RC=NN ; RP=NN ;  P9999
            // Índices: 01 23456 7 89... 13 14...

                                                                            // 09RC=03;RP=03;0045
            numLinhasConexao = Integer.parseInt(trailer.substring(5, 7));   // Ex: Pega o "03" de RC=03
            numLinhasPesos = Integer.parseInt(trailer.substring(11, 13));   // Ex: Pega o "03" de RP=03
            somaPesosTrailer = Integer.parseInt(trailer.substring(14, 18)); // Pega o "0045"

        } catch (NumberFormatException e) {
            throw new Exception("Trailer inválido: Os valores numéricos do trailer estão formatados incorretamente. Verifique o formato '09RC=NN;RP=NN;9999'.");
        }

        // Verifica quantas linhas de conexão e de pesos realmente existem no arquivo
        int contConexoes = 0;
        int contPesos = 0;
        int somaPesosCalculada = 0;

        // Percorre o meio do arquivo (entre header e trailer)
        for (int i = 1; i < linhas.size() - 1; i++) {
            String linhaAtual = linhas.get(i);

            if (linhaAtual.startsWith("01")) {
                contConexoes++;

                // Validação de formato: 01 + Letras + = + Letras (ex: 01A=B ou 01AA=BB)
                if (!linhaAtual.matches("01[A-Z]+=[A-Z]+")) {
                    throw new Exception("Resumo de conexões inválido: Formato incorreto ou uso de números na linha " + linhaAtual);
                }

            } else if (linhaAtual.startsWith("02")) {
                contPesos++;

                // Validação de formato: 02 + Letras + ; + Letras + = + Números de 4 dígitos
                if (!linhaAtual.matches("02[A-Z]+;[A-Z]+=\\d{4}")) {
                    throw new Exception("Resumo de pesos inválido: Formato incorreto na linha " + linhaAtual);
                }

                // Extrai a string do peso e converte para inteiro para somar
                String pesoStr = linhaAtual.substring(linhaAtual.indexOf("=") + 1);
                somaPesosCalculada += Integer.parseInt(pesoStr);
            }
        }

        // Teste 2.0: Confrontar número de linhas de conexão
        if (contConexoes != numLinhasConexao) {
            throw new Exception("Resumo de conexões inválido: Trailer diz " + numLinhasConexao + ", mas há " + contConexoes + " no arquivo.");
        }

        // Teste 2.1: Confrontar número de linhas de pesos
        if (contPesos != numLinhasPesos) {
            throw new Exception("Resumo de pesos inválido: Trailer diz " + numLinhasPesos + ", mas há " + contPesos + " no arquivo.");
        }

        // Validação: Soma do Header deve ser igual à do Trailer
        if (somaPesosHeader != somaPesosTrailer) {
            throw new Exception("Soma dos pesos difere: Header = " + somaPesosHeader + ", Trailer = " + somaPesosTrailer);
        }

        // Validação: Soma real das arestas deve ser igual à informada no Header/Trailer
        if (somaPesosCalculada != somaPesosHeader) {
            throw new Exception("Soma dos pesos das arestas ("+somaPesosCalculada+") difere do informado no Header/Trailer ("+somaPesosHeader+").");
        }
    }

    /**
     * Move o arquivo para a pasta destino.
     */
    private void moverArquivo(File arquivo, String pastaDestino) {
        try {
            Path origem = arquivo.toPath();
            Path destino = Paths.get(pastaDestino, arquivo.getName());
            Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Erro ao mover arquivo: " + e.getMessage());
        }
    }
}