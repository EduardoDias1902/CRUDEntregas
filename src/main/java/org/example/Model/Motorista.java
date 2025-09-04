package org.example.Model;

public class Motorista {
    private int id;
    private String nome;
    private String cnh;
    private String veiculo;
    private String cidadeBase;

    public Motorista() {}

    public Motorista(int id, String nome, String cnh, String veiculo, String cidadeBase) {
        this.id = id; this.nome = nome; this.cnh = cnh; this.veiculo = veiculo; this.cidadeBase = cidadeBase;
    }

    public Motorista(String nome, String cnh, String veiculo, String cidadeBase) {
        this.nome = nome; this.cnh = cnh; this.veiculo = veiculo; this.cidadeBase = cidadeBase;
    }

    // getters / setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }
    public String getVeiculo() { return veiculo; }
    public void setVeiculo(String veiculo) { this.veiculo = veiculo; }
    public String getCidadeBase() { return cidadeBase; }
    public void setCidadeBase(String cidadeBase) { this.cidadeBase = cidadeBase; }
}
