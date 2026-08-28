package missao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final Path RANKING_PATH = Paths.get("ranking.json");

    public static void main(String[] args) {
        Random random = new Random();
        Scanner scanner = new Scanner(System.in);
        List<RankingEntry> ranking = loadRanking(RANKING_PATH);

        System.out.println("================================================================");
        System.out.println("Missão Marte Unifor — Console");
        System.out.println("================================================================");

        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            System.out.print("Escolha uma opção: ");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    jogarPartida(scanner, random, ranking);
                    break;
                case "2":
                    System.out.println();
                    if (ranking.isEmpty()) {
                        System.out.println("Ranking vazio. Seja o primeiro a marcar pontos!");
                    } else {
                        printRanking(ranking);
                    }
                    break;
                case "3":
                    ranking = resetarRanking(scanner);
                    break;
                case "4":
                    rodando = false;
                    System.out.println("Até a próxima, comandante!");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }

        scanner.close();
        System.out.println("Fim da execução.");
    }

    private static void exibirMenu() {
        System.out.println();
        System.out.println("--- MENU PRINCIPAL ---");
        System.out.println("1. Iniciar Nova Missão");
        System.out.println("2. Visualizar Ranking Top 5");
        System.out.println("3. Resetar Histórico de Ranking");
        System.out.println("4. Sair do Jogo");
    }

    private static void jogarPartida(Scanner scanner, Random random, List<RankingEntry> ranking) {
        // int minX = -5, maxX = 5, minY = -5, maxY = 5;

        System.out.print("Digite o nome do piloto: ");
        String pilotoNome = scanner.nextLine().trim();
        if (pilotoNome.isEmpty()) {
            pilotoNome = "Piloto Anônimo";
        }

        Dificuldade dificuldade = lerDificuldade(scanner);
            System.out.print("Tamanho do mapa (-X a +X): ");
            int tamanho;
            
            try {
                tamanho = Integer.parseInt(scanner.nextLine().trim());
                if (tamanho <= 0) tamanho = 5;
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Usando mapa padrão (5).");
                tamanho = 5;
            }

            int minX = -tamanho;
            int maxX = tamanho;
            int minY = -tamanho;
            int maxY = tamanho;

        Missao missao = criarNovaMissao(random, minX, maxX, minY, maxY, dificuldade);
        Nave nave = missao.getNave();
        int score = dificuldade.getPontosIniciais();
        int movimentos = 0;
        boolean running = true;
        boolean vitoria = false;

        long tempoInicio = System.currentTimeMillis();

        while (running) {
                missao.moverInimigos(random, minX, maxX, minY, maxY);
            desenharMapa(missao, minX, maxX, minY, maxY, score, pilotoNome);
                System.out.printf("Nave em (%d,%d) | Pontos: %d | Passageiros a bordo: %d | Passageiros restantes: %d\n",
                        nave.getX(), nave.getY(), score, nave.getPassageiros().size(), missao.todosEmbarcados() ? 0 : missao.getPassageiros().size());

            if (missao.verificaColisao()) {
                    nave.perderVida();
                    if (nave.getVidas() == 0) {
                        System.out.println("Game Over!");
                break;
                    } else {
                        System.out.println("Bateu em asteroide ou inimigo! Vidas restantes: " + nave.getVidas());
                        nave.reposicionar(0, 0);
                    }
            }

            System.out.print("Para onde ir? ");
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.isEmpty()) continue;
            char cmd = line.charAt(0);
            switch (cmd) {
                case 'w': nave.moveUp(); score--; movimentos++; break;
                case 's': nave.moveDown(); score--; movimentos++; break;
                case 'a': nave.moveLeft(); score--; movimentos++; break;
                case 'd': nave.moveRight(); score--; movimentos++; break;
                case 'c': {
                    Passageiro p = missao.passagemNaPosicao();
                    if (p == null) {
                        System.out.println("Nenhum passageiro nesta posição.");
                    } else {
                        boolean ok = missao.embarcarPassageiroNaPosicao();
                        if (ok) {
                            score += p.pontuar();
                            System.out.printf("%s embarcado! +%d pontos!%n", p.getTipo(), p.pontuar());
                        } else {
                            System.out.println("Nave cheia, não foi possível embarcar.");
                        }
                    }
                    break;
                }
                case 'q':
                    running = false;
                    System.out.println("Missão abortada pelo piloto.");
                    break;
                default:
                    System.out.println("Comando desconhecido.");
            }

            if (!running) break;

            if (score <= 0) {
                System.out.println("Pontuação zerada. Missão perdida.");
                break;
            }

            if (missao.todosEmbarcados()) {
                if (nave.getX() == 0 && nave.getY() == 0) {
                    System.out.println("Nave acoplada à Plataforma de Pouso! Missão concluída com sucesso.");
                    vitoria = true;
                    running = false;
                } else {
                    System.out.println("Todos os passageiros a bordo! Retorne à Plataforma de Pouso 'L' em (0,0) para concluir a missão.");
                }
            }
        }

        long tempoFim = System.currentTimeMillis();
        long duracaoSegundos = (tempoFim - tempoInicio) / 1000;

        if (vitoria) {
            int passageirosColetados = nave.getPassageiros().size();
            exibirEstatisticas(score, movimentos, duracaoSegundos, passageirosColetados, ranking);

            if (score > 0 && isTopScore(ranking, score)) {
                String dataHora = java.time.LocalDateTime.now().toString().substring(0, 19).replace('T', ' ');
                ranking.add(new RankingEntry(pilotoNome, score, dificuldade.name(), passageirosColetados, dataHora, duracaoSegundos));
                List<RankingEntry> atualizado = ranking.stream()
                        .sorted(Comparator.comparingInt((RankingEntry e) -> e.score).reversed())
                        .limit(5)
                        .collect(Collectors.toList());
                ranking.clear();
                ranking.addAll(atualizado);
                saveRanking(RANKING_PATH, ranking);
                System.out.println("Novo ranking salvo! Você está entre os 5 maiores pontuadores.");
            }
        }

        System.out.println();
        if (!ranking.isEmpty()) {
            System.out.println("Ranking Top 5:");
            printRanking(ranking);
        } else {
            System.out.println("Ranking vazio. Seja o primeiro a marcar pontos!");
        }
    }

    private static Dificuldade lerDificuldade(Scanner scanner) {
        System.out.println("Escolha a dificuldade:");
        System.out.println("1. Fácil (5 asteroides, 30 pontos iniciais, 5 passageiros)");
        System.out.println("2. Médio (3 asteroides, 20 pontos iniciais, 3 passageiros)");
        System.out.println("3. Difícil (6 asteroides, 15 pontos iniciais, 4 passageiros)");
        System.out.print("Opção: ");
        String opcao = scanner.nextLine().trim();
        switch (opcao) {
            case "1": return Dificuldade.FACIL;
            case "3": return Dificuldade.DIFICIL;
            default: return Dificuldade.MEDIO;
        }
    }

    private static void exibirEstatisticas(int score, int movimentos, long duracaoSegundos, int passageiros, List<RankingEntry> ranking) {
        System.out.println();
        System.out.println("================ ESTATÍSTICAS DA PARTIDA ================");
        System.out.printf("Pontuação final: %d pontos%n", score);
        System.out.printf("Movimentos realizados: %d%n", movimentos);
        System.out.printf("Duração da partida: %d segundos%n", duracaoSegundos);
        System.out.printf("Passageiros resgatados: %d%n", passageiros);

        if (!ranking.isEmpty()) {
            int recorde = ranking.get(0).score;
            if (score > recorde) {
                System.out.println("🏆 Novo recorde do servidor!");
            } else {
                System.out.printf("Recorde atual: %d pontos (Piloto: %s)%n", recorde, ranking.get(0).name);
            }
        } else {
            System.out.println("Você é o primeiro a registrar uma pontuação!");
        }
        System.out.println("==========================================================");
    }

    private static List<RankingEntry> resetarRanking(Scanner scanner) {
        System.out.print("Tem certeza que deseja apagar o histórico de ranking? (s/n): ");
        String resposta = scanner.nextLine().trim().toLowerCase();
        if (resposta.equals("s") || resposta.equals("sim")) {
            try {
                Files.deleteIfExists(RANKING_PATH);
                System.out.println("Histórico de ranking resetado com sucesso!");
            } catch (IOException e) {
                System.out.println("Não foi possível apagar o ranking: " + e.getMessage());
            }
            return new ArrayList<>();
        }
        System.out.println("Operação cancelada.");
        return loadRanking(RANKING_PATH);
    }

    private static void printRanking(List<RankingEntry> ranking) {
        int position = 1;
        for (RankingEntry entry : ranking) {
            System.out.printf("%d. %s - %d pontos | Dificuldade: %s | Passageiros: %d | %s%n",
                    position++, entry.name, entry.score, entry.dificuldade, entry.passageirosColetados, entry.dataHora);
        }
    }

    private static Missao criarNovaMissao(Random random, int minX, int maxX, int minY, int maxY, Dificuldade dificuldade) {
        Nave nave = new Nave("A-1", 5);
        Missao missao = new Missao(nave);

        int qtdPassageiros = dificuldade.getQtdPassageiros();
        int qtdAsteroides = dificuldade.getQtdAsteroides();
        int qtdInimigos = dificuldade.getQtdInimigos();

        while (missao.getPassageiros().size() < qtdPassageiros) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            if (x == nave.getX() && y == nave.getY()) continue;
            if (posicaoOcupada(missao, x, y)) continue;
            int indice = missao.getPassageiros().size();
            missao.addPassageiro(criarPassageiroPolimorfico(indice, x, y));
        }

        while (missao.getAsteroides().size() < qtdAsteroides) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            if (x == nave.getX() && y == nave.getY()) continue;
            if (posicaoOcupada(missao, x, y)) continue;
            missao.addAsteroide(new Asteroide(x, y));
        }

        while (missao.getInimigos().size() < qtdInimigos) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            if (x == nave.getX() && y == nave.getY()) continue;
            if (posicaoOcupada(missao, x, y)) continue;
            missao.addInimigo(new Inimigo(x, y));
        }

        return missao;
    }

    private static Passageiro criarPassageiroPolimorfico(int indice, int x, int y) {
        switch (indice % 4) {
            case 0: return new Professor("Dr. Silva", x, y);
            case 1: return new Engenheiro("Eng. Rosa", x, y);
            case 2: return new Astronauta("Dr. Lima", x, y);
            default: return new Engenheiro("Dr. Pedro", x, y);
        }
    }

    private static boolean posicaoOcupada(Missao missao, int x, int y) {
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) return true;
        for (Passageiro p : missao.getPassageiros()) {
            if (p.getX() == x && p.getY() == y) return true;
        }
        for (Asteroide a : missao.getAsteroides()) {
            if (a.getX() == x && a.getY() == y) return true;
        }
        for (Inimigo i : missao.getInimigos()) {
            if (i.getX() == x && i.getY() == y) return true;
        }
        return false;
    }

    private static void desenharMapa(Missao missao, int minX, int maxX, int minY, int maxY, int score, String pilotoNome) {
        System.out.println();
        System.out.printf("Mapa da Missão (Pontos: %d) - Piloto: %s%n", score, pilotoNome);
        System.out.print("    ");
        for (int x = minX; x <= maxX; x++) {
            System.out.printf(" %2d", x);
        }
        System.out.println();
        System.out.print("    ");
        for (int x = minX; x <= maxX; x++) {
            System.out.print(" __");
        }
        System.out.println();

        for (int y = minY; y <= maxY; y++) {
            System.out.printf("%3d|", y);
            for (int x = minX; x <= maxX; x++) {
                char symbol = '.';
                if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
                    symbol = '^';
                } else {
                    for (Passageiro p : missao.getPassageiros()) {
                        if (p.getX() == x && p.getY() == y) {
                            if (p instanceof Engenheiro) {
                                symbol = 'E';
                            } else if (p instanceof Professor) {
                                symbol = 'P';
                            } else if (p instanceof Astronauta) {
                                symbol = 'X';
                            }
                            break;
                        }
                    }
                    if (symbol == '.') {
                        for (Asteroide a : missao.getAsteroides()) {
                            if (a.getX() == x && a.getY() == y) {
                                symbol = '@';
                                break;
                            }
                        }
                    }
                    if (symbol == '.') {
                        for (Inimigo i : missao.getInimigos()) {
                            if (i.getX() == x && i.getY() == y) {
                                symbol = '!';
                                break;
                            }
                        }
                    }
                }
                System.out.printf(" %2c", symbol);
            }
            System.out.println();
        }

        System.out.println("Legenda: ^=Nave, X=Astronauta, P=Professor, E=Engenheiro, @=Asteroide, !=Inimigo, .=Vazio");
        System.out.println("Resumo de comandos: w(cima)/s(baixo)/a(esquerda)/d(direita) mover, c embarcar, q sair");
        System.out.println("Passageiros restantes:");
        for (Passageiro p : missao.getPassageiros()) {
            System.out.printf(" - %s (%s) em (%d,%d)\n", p.getNome(), p.getTipo(), p.getX(), p.getY());
        }
        System.out.println();
    }

    private static boolean isTopScore(List<RankingEntry> ranking, int score) {
        if (ranking.size() < 5) {
            return true;
        }
        return score > ranking.get(ranking.size() - 1).score;
    }

    private static List<RankingEntry> loadRanking(Path path) {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
            return parseRankingJson(json);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void saveRanking(Path path, List<RankingEntry> ranking) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < ranking.size(); i++) {
            RankingEntry entry = ranking.get(i);
            builder.append("{\"name\":\"")
                    .append(entry.name.replace("\"", "\\\""))
                    .append("\",\"score\":")
                    .append(entry.score)
                    .append(",\"dificuldade\":\"")
                    .append(entry.dificuldade)
                    .append("\",\"passageirosColetados\":")
                    .append(entry.passageirosColetados)
                    .append(",\"dataHora\":\"")
                    .append(entry.dataHora)
                    .append("\",\"tempoJogo\":")
                    .append(entry.tempoJogo)
                    .append("}");
            if (i < ranking.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        try {
            Files.write(path, builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o ranking: " + e.getMessage());
        }
    }

    private static List<RankingEntry> parseRankingJson(String json) {
        List<RankingEntry> ranking = new ArrayList<>();
        if (json.isEmpty() || json.equals("[]")) {
            return ranking;
        }
        json = json.trim();
        if (json.startsWith("[")) {
            json = json.substring(1);
        }
        if (json.endsWith("]")) {
            json = json.substring(0, json.length() - 1);
        }

        int index = 0;
        while (index < json.length()) {
            int start = json.indexOf('{', index);
            if (start < 0) break;
            int end = json.indexOf('}', start);
            if (end < 0) break;
            String object = json.substring(start + 1, end);
            String name = null;
            Integer score = null;
            String dificuldade = "MEDIO";
            int passageirosColetados = 0;
            String dataHora = "";
            long tempoJogo = 0;

            for (String part : object.split(",")) {
                String[] pair = part.split(":", 2);
                if (pair.length != 2) continue;
                String key = pair[0].trim().replaceAll("\"", "");
                String value = pair[1].trim();
                if (key.equals("name")) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        name = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                    }
                } else if (key.equals("score")) {
                    try {
                        score = Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {
                    }
                } else if (key.equals("dificuldade")) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        dificuldade = value.substring(1, value.length() - 1);
                    }
                } else if (key.equals("passageirosColetados")) {
                    try {
                        passageirosColetados = Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {
                    }
                } else if (key.equals("dataHora")) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        dataHora = value.substring(1, value.length() - 1);
                    }
                } else if (key.equals("tempoJogo")) {
                    try {
                        tempoJogo = Long.parseLong(value);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if (name != null && score != null) {
                ranking.add(new RankingEntry(name, score, dificuldade, passageirosColetados, dataHora, tempoJogo));
            }
            index = end + 1;
        }

        ranking.sort(Comparator.comparingInt((RankingEntry e) -> e.score).reversed());
        return ranking;
    }

    private static class RankingEntry {
        private final String name;
        private final int score;
        private final String dificuldade;
        private final int passageirosColetados;
        private final String dataHora;
        private final long tempoJogo;

        private RankingEntry(String name, int score, String dificuldade, int passageirosColetados, String dataHora, long tempoJogo) {
            this.name = name;
            this.score = score;
            this.dificuldade = dificuldade;
            this.passageirosColetados = passageirosColetados;
            this.dataHora = dataHora;
            this.tempoJogo = tempoJogo;
        }
    }
}