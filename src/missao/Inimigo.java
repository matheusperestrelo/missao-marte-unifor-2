package missao;

public class Inimigo {
    private int x;
    private int y;

    public Inimigo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void moverEmDirecaoA(Nave n) {
        if (n.getX() > x) x++;
        else if (n.getX() < x) x--;

        if (n.getY() > y) y++;
        else if (n.getY() < y) y--;
    }

    public boolean colideCom(Nave n) {
        return n.getX() == x && n.getY() == y;
    }
}

