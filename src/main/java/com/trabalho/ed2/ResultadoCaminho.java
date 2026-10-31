package com.trabalho.ed2;

import java.util.List;

public class ResultadoCaminho {
    private List<Integer> caminho;
    private int custo;

    public ResultadoCaminho(List<Integer> caminho, int custo) {
        this.caminho = caminho;
        this.custo = custo;
    }

    // --- Getters e Setters ---

    public List<Integer> getCaminho() {
        return caminho;
    }

    public void setCaminho(List<Integer> caminho) {
        this.caminho = caminho;
    }

    public int getCusto() {
        return custo;
    }

    public void setCusto(int custo) {
        this.custo = custo;
    }
}