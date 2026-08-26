package missao;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Missao {
    private Nave nave;
    private List<Passageiro> passageiros = new ArrayList<>();
    private List<Asteroide> asteroides = new ArrayList<>();
    private List<Inimigo> inimigos = new ArrayList<>();

    public List<Inimigo> getInimigos() { return inimigos; }
    public void addInimigo(Inimigo i) { inimigos.add(i); }


    public Missao(Nave nave) {
        this.nave = nave;
    }

    public Nave getNave() {
        return nave;
    }

    public java.util.List<Passageiro> getPassageiros() {
        return passageiros;
    }

    public java.util.List<Asteroide> getAsteroides() {
        return asteroides;
    }

    public void addPassageiro(Passageiro p) { passageiros.add(p); }
    public void addAsteroide(Asteroide a) { asteroides.add(a); }

    public boolean verificaColisao() {
        for (Asteroide a : asteroides) {
            if (a.colideCom(nave)) return true;
        }
        for (Inimigo i : inimigos) {
            if (i.colideCom(nave)) return true;
        }
        return false;
    }

    public void moverInimigos() {
        for (Inimigo i : inimigos) {
            i.moverEmDirecaoA((nave));
        }
    }

    public Passageiro passagemNaPosicao() {
        for (Passageiro p : passageiros) {
            if (p.getX() == nave.getX() && p.getY() == nave.getY()) return p;
        }
        return null;
    }

    public boolean embarcarPassageiroNaPosicao() {
        Iterator<Passageiro> it = passageiros.iterator();
        while (it.hasNext()) {
            Passageiro p = it.next();
            if (p.getX() == nave.getX() && p.getY() == nave.getY()) {
                boolean ok = nave.embarcar(p);
                if (ok) it.remove();
                return ok;
            }
        }
        return false;
    }

    public boolean todosEmbarcados() { return passageiros.isEmpty(); }
}
