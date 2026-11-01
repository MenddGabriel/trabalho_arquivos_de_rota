package com.trabalho.ed2;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TelaVisivel extends JDialog {

    private GerenciadorArquivos gerenciador;

    private JTextField txtArquivo = new JTextField(30);
    private DefaultTableModel modelo;
    private JTable tabela;

    private JTextField txtOrigem = new JTextField(8);
    private JTextField txtDestino = new JTextField(8);
    private JTextField txtKm = new JTextField(5);

    private JComboBox<String> cbDe = new JComboBox<>();
    private JComboBox<String> cbPara = new JComboBox<>();
    private JLabel lblResultado = new JLabel(" ");

    private int proximoCodigo = 1;

    public TelaVisivel(JFrame dono, GerenciadorArquivos gerenciador) {
        super(dono, "Busca de menor caminho", true);
        this.gerenciador = gerenciador;
        setLayout(new BorderLayout(5, 5));

        // Parte de cima: escolher o arquivo
        JPanel painelArquivo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBuscar = new JButton("Buscar");
        JButton btnCarregar = new JButton("Carregar");
        painelArquivo.add(new JLabel("Arquivo:"));
        painelArquivo.add(txtArquivo);
        painelArquivo.add(btnBuscar);
        painelArquivo.add(btnCarregar);
        add(painelArquivo, BorderLayout.NORTH);

        // Tabela com as rotas
        modelo = new DefaultTableModel(new String[]{"Código", "Cidade Origem", "Cidade Destino", "KM"}, 0) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Parte de baixo: adicionar rota na mão e processar
        JPanel painelAdicionar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdicionar = new JButton("Adicionar");
        JButton btnRemover = new JButton("Remover");
        painelAdicionar.add(new JLabel("Origem:"));
        painelAdicionar.add(txtOrigem);
        painelAdicionar.add(new JLabel("Destino:"));
        painelAdicionar.add(txtDestino);
        painelAdicionar.add(new JLabel("KM:"));
        painelAdicionar.add(txtKm);
        painelAdicionar.add(btnAdicionar);
        painelAdicionar.add(btnRemover);

        JPanel painelProcessar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnProcessar = new JButton("PROCESSAR");
        painelProcessar.add(new JLabel("De:"));
        painelProcessar.add(cbDe);
        painelProcessar.add(new JLabel("Para:"));
        painelProcessar.add(cbPara);
        painelProcessar.add(btnProcessar);

        JPanel painelResultado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelResultado.add(lblResultado);

        JPanel painelBaixo = new JPanel(new GridLayout(3, 1));
        painelBaixo.add(painelAdicionar);
        painelBaixo.add(painelProcessar);
        painelBaixo.add(painelResultado);
        add(painelBaixo, BorderLayout.SOUTH);

        btnBuscar.addActionListener(e -> buscarArquivo());
        btnCarregar.addActionListener(e -> carregarArquivo());
        txtArquivo.addActionListener(e -> carregarArquivo()); // Enter no campo também carrega
        btnAdicionar.addActionListener(e -> adicionarRota());
        btnRemover.addActionListener(e -> removerRota());
        btnProcessar.addActionListener(e -> processar());

        setSize(750, 500);
        setLocationRelativeTo(dono);
    }

    private void buscarArquivo() {
        JFileChooser escolher = new JFileChooser(gerenciador.getPastaRaiz());
        escolher.setFileFilter(new FileNameExtensionFilter("Arquivos de rota (*.txt)", "txt"));

        if (escolher.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtArquivo.setText(escolher.getSelectedFile().getAbsolutePath());
            carregarArquivo();
        }
    }

    private void carregarArquivo() {
        File arquivo = new File(txtArquivo.getText().trim());

        if (!arquivo.exists()) {
            JOptionPane.showMessageDialog(this, "Arquivo não encontrado.");
            return;
        }

        // Usa as mesmas validações do processamento dos arquivos
        try {
            gerenciador.validarArquivo(arquivo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Arquivo inválido: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Grafo grafo = gerenciador.getGrafo();
        List<String> nos = gerenciador.getNos();

        modelo.setRowCount(0);
        proximoCodigo = 1;

        // Cada aresta do grafo vira uma linha da tabela
        for (int i = 0; i < nos.size(); i++) {
            for (int j = i + 1; j < nos.size(); j++) {
                if (grafo.getCusto(i, j) > 0) {
                    modelo.addRow(new Object[]{proximoCodigo++, nos.get(i), nos.get(j), grafo.getCusto(i, j)});
                }
            }
        }

        atualizarCidades();
        lblResultado.setText(" ");
    }

    private void adicionarRota() {
        String origem = txtOrigem.getText().trim();
        String destino = txtDestino.getText().trim();

        if (origem.isEmpty() || destino.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha a origem e o destino.");
            return;
        }

        if (origem.equalsIgnoreCase(destino)) {
            JOptionPane.showMessageDialog(this, "Origem e destino não podem ser a mesma cidade.");
            return;
        }

        int km;
        try {
            km = Integer.parseInt(txtKm.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "KM inválido.");
            return;
        }

        if (km < 1) {
            JOptionPane.showMessageDialog(this, "O KM tem que ser maior que zero.");
            return;
        }

        modelo.addRow(new Object[]{proximoCodigo++, origem, destino, km});
        atualizarCidades();

        txtOrigem.setText("");
        txtDestino.setText("");
        txtKm.setText("");
        txtOrigem.requestFocus();
    }

    private void removerRota() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha da tabela.");
            return;
        }

        modelo.removeRow(linha);
        atualizarCidades();
    }

    private void processar() {
        if (modelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Carregue um arquivo ou adicione as rotas.");
            return;
        }

        String de = (String) cbDe.getSelectedItem();
        String para = (String) cbPara.getSelectedItem();
        List<String> cidades = getCidades();

        // Monta o grafo com o que está na tabela (posição da cidade na lista = vértice)
        Grafo grafo = new Grafo(cidades.size());
        for (int i = 0; i < modelo.getRowCount(); i++) {
            int origem = cidades.indexOf((String) modelo.getValueAt(i, 1));
            int destino = cidades.indexOf((String) modelo.getValueAt(i, 2));
            int km = (Integer) modelo.getValueAt(i, 3);
            grafo.criaArestar(origem, destino, km);
        }

        ResultadoCaminho resultado = grafo.caminhoMinimo(cidades.indexOf(de), cidades.indexOf(para));

        if (resultado.getCaminho().isEmpty()) {
            lblResultado.setText("Não existe caminho entre " + de + " e " + para + ".");
            return;
        }

        String caminho = "";
        for (int vertice : resultado.getCaminho()) {
            if (!caminho.isEmpty()) {
                caminho += " -> ";
            }
            caminho += cidades.get(vertice);
        }

        lblResultado.setText("Menor caminho: " + caminho + "   |   Total: " + resultado.getCusto() + " km");
    }

    // Lista das cidades que aparecem na tabela, sem repetir
    private List<String> getCidades() {
        List<String> cidades = new ArrayList<>();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            String origem = (String) modelo.getValueAt(i, 1);
            String destino = (String) modelo.getValueAt(i, 2);
            if (!cidades.contains(origem)) cidades.add(origem);
            if (!cidades.contains(destino)) cidades.add(destino);
        }
        return cidades;
    }

    private void atualizarCidades() {
        cbDe.removeAllItems();
        cbPara.removeAllItems();
        for (String cidade : getCidades()) {
            cbDe.addItem(cidade);
            cbPara.addItem(cidade);
        }
    }
}
