package com.trabalho.ed2;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TelaPrincipal tela = new TelaPrincipal();
            tela.setVisible(true);

            // Lê o config (ou abre a tela de configurações se for a primeira vez)
            tela.iniciar();
        });
    }
}
