package missao;

public enum Dificuldade {
    FACIL(5, 30, 5, 1),
    NORMAL(3, 20, 3, 2),
    DIFICIL(6, 15, 4, 3);

    private final int qtdAsteroides;
    private final int pontosIniciais;
    private final int qtdPassageiros;
    private final int qtdInimigos;

    Dificuldade(int qtdAsteroides, int pontosIniciais, int qtdPassageiros, int qtdInimigos) {
        this.qtdAsteroides = qtdAsteroides;
        this.pontosIniciais = pontosIniciais;
        this.qtdPassageiros = qtdPassageiros;
        this.qtdInimigos = qtdInimigos;
    }

    public int getQtdAsteroides() { return qtdAsteroides; }
    public int getPontosIniciais() { return pontosIniciais; }
    public int getQtdPassageiros() { return qtdPassageiros; }
    public int getQtdInimigos() { return qtdInimigos; }
}
