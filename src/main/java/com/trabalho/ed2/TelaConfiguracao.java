package com.trabalho.ed2;

import javax.swing.*;
import java.awt.*;

public class TelaConfiguracao extends JDialog {

    private JTextField txtPasta = new JTextField(25);
    private JTextField txtSucesso = new JTextField(25);
    private JTextField txtErro = new JTextField(25);
    private JCheckBox chkAutomatica = new JCheckBox("Rota automática");

    private boolean salvou = false;

    public TelaConfiguracao(JFrame dono, GerenciadorArquivos gerenciador) {
        super(dono, "Configurações", true);

        JPanel painel = new JPanel(new GridLayout(5, 2, 5, 5));
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painel.add(new JLabel("Pasta:"));
        painel.add(txtPasta);
        painel.add(new JLabel("Sucesso:"));
        painel.add(txtSucesso);
        painel.add(new JLabel("Erro:"));
        painel.add(txtErro);
        painel.add(new JLabel(""));
        painel.add(chkAutomatica);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnCancelar = new JButton("Cancelar");
        painel.add(btnSalvar);
        painel.add(btnCancelar);
        add(painel);

        // Já mostra o que está configurado
        txtPasta.setText(gerenciador.getPastaRaiz());
        txtSucesso.setText(gerenciador.getPastaProcessado());
        txtErro.setText(gerenciador.getPastaNaoProcessado());
        chkAutomatica.setSelected(gerenciador.isUsarRotaAutomatica());

        btnSalvar.addActionListener(e -> {
            String pasta = txtPasta.getText().trim();
            String sucesso = txtSucesso.getText().trim();
            String erro = txtErro.getText().trim();

            if (pasta.isEmpty() || sucesso.isEmpty() || erro.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha todas as pastas.");
                return;
            }

            try {
                gerenciador.salvarConfiguracao(pasta, sucesso, erro, chkAutomatica.isSelected());
                salvou = true;
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(dono);
    }

    public boolean isSalvou() {
        return salvou;
    }
}
