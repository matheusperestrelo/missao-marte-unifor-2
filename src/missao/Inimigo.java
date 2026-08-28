package missao;

import java.util.Random;

public class Inimigo {
    private int x;
    private int y;

    public Inimigo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void moverAleatorio(Random r, int minX, int maxX, int minY, int maxY) {
        x = Math.max(minX, Math.min(maxX, x + r.nextInt(3) - 1));
        y = Math.max(minY, Math.min(maxY, y + r.nextInt(3) - 1));
    }

    public boolean colideCom(Nave n) {
        return n.getX() == x && n.getY() == y;
    }
}
