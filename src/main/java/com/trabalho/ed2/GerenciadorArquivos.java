package com.trabalho.ed2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorArquivos {

    private String pastaRaiz;
    private String pastaProcessado;
    private String pastaNaoProcessado;

    // Grafo e nomes dos nós do último arquivo lido (índice da lista = vértice do grafo)
    private Grafo grafo;
    private List<String> nos;

    // Controle da rota automática (thread que roda em background)
    private Thread threadAutomatica;
    private volatile boolean rotaAutomatica = false;

    // Opção "Rota automática" da tela de configuração
    private boolean usarRotaAutomatica = false;

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
        linhas.removeIf(String::isBlank);


        // Teste 1.3 e 1.4: Arquivo em branco ou faltando a linha 2
        // Define o conteúdo padrão esperado
        String linha1Esperada = "Processado=C:\\Teste\\Processado";
        String linha2Esperada = "Nao Processado=C:\\Teste\\NaoProcessado";

        // Só recria se estiver vazio ou incompleto. Se tiver linha errada (ex: '@'), cai na exceção lá embaixo (Teste 1.5)
        if (linhas.size() < 2) {
            System.out.println("Arquivo config.txt em branco ou incompleto. Corrigindo...");

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
            } else if (linha.startsWith("Pasta=")) {
                this.pastaRaiz = linha.split("=")[1];
            } else if (linha.startsWith("Rota Automatica=")) {
                this.usarRotaAutomatica = linha.split("=")[1].equalsIgnoreCase("Sim");
            } else if (!linha.trim().isEmpty()) {
                throw new Exception("EXCEÇÃO: Formato inválido no config.txt (falta o '=').");
            }
        }

        // Cria os diretórios se não existirem
        new File(pastaRaiz).mkdirs();
        new File(pastaProcessado).mkdirs();
        new File(pastaNaoProcessado).mkdirs();

        // Com rota automática fica verificando a pasta, senão processa o que tiver uma vez só
        if (usarRotaAutomatica) {
            iniciarRotaAutomatica();
        } else {
            pararRotaAutomatica();
            processarTodosArquivos();
        }
    }

    public boolean existeConfiguracao() {
        return new File("C:\\Teste\\Configuracao\\config.txt").exists();
    }

    /**
     * Grava o config.txt com o que foi preenchido na tela de configurações.
     */
    public void salvarConfiguracao(String pasta, String sucesso, String erro, boolean automatica) throws Exception {
        File arquivoConfig = new File("C:\\Teste\\Configuracao\\config.txt");
        arquivoConfig.getParentFile().mkdirs();

        try (java.io.FileWriter fw = new java.io.FileWriter(arquivoConfig, false);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {

            bw.write("Processado=" + sucesso);
            bw.newLine();
            bw.write("Nao Processado=" + erro);
            bw.newLine();
            bw.write("Pasta=" + pasta);
            bw.newLine();
            bw.write("Rota Automatica=" + (automatica ? "Sim" : "Nao"));

        } catch (java.io.IOException e) {
            throw new Exception("EXCEÇÃO: Não foi possível salvar o config.txt.", e);
        }
    }

    public String getPastaRaiz() {
        return pastaRaiz;
    }

    public String getPastaProcessado() {
        return pastaProcessado;
    }

    public String getPastaNaoProcessado() {
        return pastaNaoProcessado;
    }

    public boolean isUsarRotaAutomatica() {
        return usarRotaAutomatica;
    }

    /**
     * Busca todos os arquivos rotaNN.txt na pasta raiz e os processa.
     * Requisito do PDF: "O sistema deverá permitir a leitura de vários arquivos... e tratar TODOS ao mesmo tempo."
     */
    public synchronized void processarTodosArquivos() {
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
     * Rota automática: verifica a pasta raiz a cada 10 segundos e processa os arquivos que chegarem (Teste 2.3).
     */
    public void iniciarRotaAutomatica() {
        if (rotaAutomatica) {
            return; // já está rodando
        }

        rotaAutomatica = true;
        threadAutomatica = new Thread(() -> {
            while (rotaAutomatica) {
                processarTodosArquivos();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break; // pediram pra parar
                }
            }
        });
        System.out.println("Rota automática ligada. Verificando a pasta " + pastaRaiz + " a cada 10 segundos...");
        threadAutomatica.start();
    }

    public void pararRotaAutomatica() {
        if (!rotaAutomatica) {
            return; // já está parada
        }

        rotaAutomatica = false;
        if (threadAutomatica != null) {
            threadAutomatica.interrupt();
        }
        System.out.println("Rota automática desligada.");
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
     * Também é usado pela tela Visível para carregar um arquivo na tabela.
     */
    public void validarArquivo(File arquivo) throws Exception {
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

        int numNosHeader;
        int somaPesosHeader;
        try {
            // Pega o NN (posições 2 e 3) e os 5 últimos caracteres do header
            numNosHeader = Integer.parseInt(header.substring(2, 4));
            somaPesosHeader = Integer.parseInt(header.substring(4, 9));
        } catch (NumberFormatException e) {
            throw new Exception("Header inválido: NN ou SP não é um número válido.");
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

        List<String> nosArquivo = new ArrayList<>();
        List<String> conexoes = new ArrayList<>();
        List<String[]> arestas = new ArrayList<>();

        // Percorre o meio do arquivo (entre header e trailer)
        for (int i = 1; i < linhas.size() - 1; i++) {
            String linhaAtual = linhas.get(i);

            if (linhaAtual.startsWith("01")) {
                contConexoes++;

                // Validação de formato: 01 + Letras + = + Letras (ex: 01A=B ou 01AA=BB)
                if (!linhaAtual.matches("01[A-Z]{1,2}=[A-Z]{1,2}")) {
                    throw new Exception("Resumo de conexões inválido: Formato incorreto ou uso de números na linha " + linhaAtual);
                }

                // Guarda a conexão e os nós que aparecem nela
                String[] partes = linhaAtual.substring(2).split("=");
                conexoes.add(partes[0] + "=" + partes[1]);
                if (!nosArquivo.contains(partes[0])) nosArquivo.add(partes[0]);
                if (!nosArquivo.contains(partes[1])) nosArquivo.add(partes[1]);

            } else if (linhaAtual.startsWith("02")) {
                contPesos++;

                // Validação de formato: 02 + Letras + ; + Letras + = + Números de 4 dígitos
                if (!linhaAtual.matches("02[A-Z]{1,2};[A-Z]{1,2}=\\d{4}")) {
                    throw new Exception("Resumo de pesos inválido: Formato incorreto na linha " + linhaAtual);
                }

                // Separa origem, destino e peso (ex: 02A;B=0010 -> A, B, 0010)
                String[] partes = linhaAtual.substring(2).split("[;=]");

                // O peso tem que ser de uma conexão que existe no resumo de conexões
                if (!conexoes.contains(partes[0] + "=" + partes[1]) && !conexoes.contains(partes[1] + "=" + partes[0])) {
                    throw new Exception("Resumo de pesos inválido: A conexão " + partes[0] + "=" + partes[1] + " não existe no resumo de conexões.");
                }

                somaPesosCalculada += Integer.parseInt(partes[2]);
                arestas.add(partes);

            } else {
                throw new Exception("Linha inválida no arquivo: " + linhaAtual);
            }
        }

        // Teste 1.6: NN do header tem que bater com a quantidade de nós das conexões
        if (nosArquivo.size() != numNosHeader) {
            throw new Exception("Número totais de nós inválido. Header = " + numNosHeader + ", encontrados no arquivo = " + nosArquivo.size());
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

        // Teste 1.7: Soma real das arestas deve ser igual à informada no Header/Trailer
        if (somaPesosCalculada != somaPesosHeader) {
            throw new Exception("Soma dos pesos difere (Valor do Registro HEADER = " + somaPesosHeader + " e Soma dos Pesos = " + somaPesosCalculada + ")");
        }

        // Arquivo válido: monta o grafo (o nó "A" vira o índice dele na lista nosArquivo)
        Grafo grafoArquivo = new Grafo(nosArquivo.size());
        for (String[] aresta : arestas) {
            grafoArquivo.criaArestar(nosArquivo.indexOf(aresta[0]), nosArquivo.indexOf(aresta[1]), Integer.parseInt(aresta[2]));
        }

        this.grafo = grafoArquivo;
        this.nos = nosArquivo;
    }

    public Grafo getGrafo() {
        return grafo;
    }

    public List<String> getNos() {
        return nos;
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