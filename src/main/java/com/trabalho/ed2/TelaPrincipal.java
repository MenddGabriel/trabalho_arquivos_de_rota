package com.trabalho.ed2;

import javax.swing.*;

public class TelaPrincipal extends JFrame {

    private GerenciadorArquivos gerenciador = new GerenciadorArquivos();

    public TelaPrincipal() {
        setTitle("Arquivos de Rota");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Menu com as opções Visível, Configurações e Sair
        JMenuBar barra = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        JMenuItem itemVisivel = new JMenuItem("Visível");
        JMenuItem itemConfiguracoes = new JMenuItem("Configurações");
        JMenuItem itemSair = new JMenuItem("Sair");

        menu.add(itemVisivel);
        menu.add(itemConfiguracoes);
        menu.addSeparator();
        menu.add(itemSair);
        barra.add(menu);
        setJMenuBar(barra);

        itemVisivel.addActionListener(e -> new TelaVisivel(this, gerenciador).setVisible(true));
        itemConfiguracoes.addActionListener(e -> abrirConfiguracoes());
        itemSair.addActionListener(e -> System.exit(0));

        add(new JLabel("Escolha uma opção no menu.", SwingConstants.CENTER));
    }

    public void iniciar() {
        // Primeira vez usando: abre a tela de configurações sozinha
        if (!gerenciador.existeConfiguracao()) {
            JOptionPane.showMessageDialog(this, "Primeira vez usando o sistema. Preencha as configurações.");
            new TelaConfiguracao(this, gerenciador).setVisible(true);
        }

        carregarConfiguracao();
    }

    private void abrirConfiguracoes() {
        TelaConfiguracao tela = new TelaConfiguracao(this, gerenciador);
        tela.setVisible(true);

        if (tela.isSalvou()) {
            carregarConfiguracao();
        }
    }

    private void carregarConfiguracao() {
        try {
            gerenciador.inicializarConfiguracao();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro na configuração", JOptionPane.ERROR_MESSAGE);
        }
    }
}
