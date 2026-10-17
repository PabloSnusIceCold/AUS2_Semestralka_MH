// Trieda TreeTester.java bola vygenerovaná pomocou AI (Claude Sonnet 5.5)

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * CLI tester pre K-D strom a Quad strom (operacie Insert a Find).
 *
 * Vsetky vlozene body sa ukladaju do referencneho ArrayListu. Vysledok Find sa
 * porovnava s linearnym prehladanim tohto zoznamu, pricom sa musia zhodovat
 * konkretne objekty (identita), nie iba pocet. Pocitaju sa volania porovnavacej
 * funkcie, aby sa overilo, ze Find vyuziva priestorove delenie a neprehlada
 * vsetky zaznamy.
 *
 * Pouzitie: java TreeTester [--structure kd|quad|all] [--ops N] [--pInsert P]
 * [--range R] [--pDup P] [--maxDepth D] [--verifyEvery N] [--seed S]
 */
public class TreeTester {
    // nastavenia z prikazoveho riadku
    static String structure = "all";
    static int ops = 100_000;
    static double pInsertOption = -1; // -1 = prednastavene scenare
    static int rangeOption = 1000;
    static double pDup = 0.05;
    static int maxDepth = 20;
    static long seed = 12345;
    static int verifyEveryOption = 100000; // --verifyEvery: overuje sa kazdy N-ty Find v nahodnych operaciach

    // overovanie Find: kazdy verifyEvery-ty Find sa porovna s referenciou
    static int verifyEvery = 1;
    static int findCounter = 0;

    // aktualne testovany strom a referencne zaznamy
    static boolean useKd;
    static KDTree<Point2D> kd;
    static QuadTree<Point2D> quad;
    static long calls; // pocet volani porovnavacej funkcie
    static ArrayList<Point2D> ref = new ArrayList<>();
    static int[] mark = new int[1024];
    static int stamp = 1;

    static int passed;
    static int failed;

    // ---------------------------------------------------------------- stromy

    /**
     * Vytvori prazdny testovany strom pre suradnice z [0, range] a vyprazdni
     * referenciu.
     */
    static void newTree(int range) {
        ref.clear();
        if (useKd) {
            kd = new KDTree<>(2, (a, b, d) -> {
                calls++;
                return d == 0 ? Integer.compare(a.x, b.x) : Integer.compare(a.y, b.y);
            });
            return;
        }
        QuadComparator<Point2D> comparator = new QuadComparator<>() {
            public int compare(Point2D a, Point2D b, int d) {
                calls++;
                return d == 0 ? Integer.compare(a.x, b.x) : Integer.compare(a.y, b.y);
            }

            public double difference(Point2D a, Point2D b, int d) {
                return d == 0 ? (double) b.x - a.x : (double) b.y - a.y;
            }

            public Point2D middle(Point2D lower, Point2D upper) {
                return new Point2D(lower.x + (upper.x - lower.x) / 2, lower.y + (upper.y - lower.y) / 2, -1);
            }

            public Point2D combine(Point2D xSource, Point2D ySource) {
                return new Point2D(xSource.x, ySource.y, -1);
            }
        };
        quad = new QuadTree<>(comparator, new Point2D(0, 0, -1), new Point2D(range, range, -1), maxDepth);
    }

    /**
     * Vlozi novy bod do stromu aj do referencneho zoznamu (id = pozicia v zozname).
     */
    static Point2D add(int x, int y) {
        Point2D p = new Point2D(x, y, ref.size());
        ref.add(p);
        if (useKd) {
            kd.insert(p);
        } else {
            quad.insert(p);
        }
        return p;
    }

    /**
     * Spusti Find na strome a porovna ho s referenciou: musia sa zhodovat
     * konkretne objekty, ziadny nesmie chybat, byt navyse ani sa opakovat.
     *
     * @return pocet porovnani vykonanych stromom pocas Find
     */
    static long find(int x1, int y1, int x2, int y2, String ctx) {
        Point2D lower = new Point2D(x1, y1, -1);
        Point2D upper = new Point2D(x2, y2, -1);
        calls = 0;
        List<Point2D> result = useKd ? kd.find(lower, upper) : quad.find(lower, upper);
        long used = calls;
        String where = ctx + " [" + x1 + "," + y1 + "]-[" + x2 + "," + y2 + "]: ";
        if (result == null) {
            throw new AssertionError(where + "find vratil null");
        }
        if (++findCounter % verifyEvery != 0) { // zvysok po deleni ak je 0 tak sa urobi overenie
            return used;
        }
        if (mark.length < ref.size()) {
            mark = new int[Math.max(ref.size(), mark.length * 2)];
        }
        stamp += 2;
        int expected = 0;
        for (Point2D p : ref) {
            if (p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2) {
                mark[p.id] = stamp; // stav "ocakavany" - bod ma byt vo vysledku a strom ho este nevratil
                expected++;
            }
        }
        if (result.size() != expected) {
            throw new AssertionError(where + "pocet " + result.size() + " != ocakavany " + expected);
        }
        for (Point2D e : result) {
            if (e == null || e.id < 0 || e.id >= ref.size() || ref.get(e.id) != e) {
                throw new AssertionError(where + "vrateny objekt nebol vlozeny: " + e);
            }
            if (mark[e.id] != stamp) { // bod nema byt vo vysledku (nie je v intervale)
                throw new AssertionError(where + "objekt " + e + " je mimo intervalu alebo je vrateny viackrat");
            }
            mark[e.id] = stamp + 1; // stav "vrateny" - bod ma byt vo vysledku a strom ho uz vratil
        }
        return used;
    }

    static void findAll(int range, String ctx) {
        find(0, 0, range, range, ctx + " (cela oblast)");
    }

    // ------------------------------------------------------ nahodne generovanie

    /**
     * Nahodny bod: bezne rovnomerny, obcas na hranici rozsahu alebo duplikat
     * existujuceho.
     */
    static int[] randomPoint(Random rnd, int range, double dup) {
        if (!ref.isEmpty() && rnd.nextDouble() < dup) {
            Point2D p = ref.get(rnd.nextInt(ref.size()));
            return new int[] { p.x, p.y };
        }
        // v 3% pripadov je na kraji rozsahu bud 0 alebo range
        int x = rnd.nextDouble() < 0.03 ? (rnd.nextBoolean() ? 0 : range) : rnd.nextInt(range + 1);
        int y = rnd.nextDouble() < 0.03 ? (rnd.nextBoolean() ? 0 : range) : rnd.nextInt(range + 1);
        return new int[] { x, y };
    }

    /**
     * Nahodny interval: maly, stredny, bodovy, na hraniciach bodov, ciara,
     * presahujuci, otoceny, cely.
     */
    static int[] randomInterval(Random rnd, int range) {
        double kind = rnd.nextDouble();
        int x1 = rnd.nextInt(range + 1);
        int y1 = rnd.nextInt(range + 1);
        int x2;
        int y2;
        if (kind < 0.40 || kind >= 0.955) { // maly
            x2 = x1 + rnd.nextInt(Math.max(1, range / 50) + 1);
            y2 = y1 + rnd.nextInt(Math.max(1, range / 50) + 1);
        } else if (kind < 0.62) { // stredny
            x2 = x1 + rnd.nextInt(range / 5 + 2);
            y2 = y1 + rnd.nextInt(range / 5 + 2);
        } else if (kind < 0.72 && !ref.isEmpty()) { // bodovy interval na existujucom bode
            Point2D p = ref.get(rnd.nextInt(ref.size()));
            x1 = x2 = p.x;
            y1 = y2 = p.y;
        } else if (kind < 0.84 && !ref.isEmpty()) { // hranice presne na suradniciach bodov
            Point2D p = ref.get(rnd.nextInt(ref.size()));
            Point2D q = ref.get(rnd.nextInt(ref.size()));
            x1 = Math.min(p.x, q.x);
            y1 = Math.min(p.y, q.y);
            x2 = Math.min(Math.max(p.x, q.x), x1 + range / 4);
            y2 = Math.min(Math.max(p.y, q.y), y1 + range / 4);
        } else if (kind < 0.90) { // tenka ciara (vodorovna alebo zvisla)
            x2 = x1 + rnd.nextInt(2);
            y2 = y1 + rnd.nextInt(range / 3 + 1);
            if (rnd.nextBoolean()) {
                int t = x1;
                x1 = y1;
                y1 = t;
                t = x2;
                x2 = y2;
                y2 = t;
            }
        } else if (kind < 0.93) { // presahuje rozsah
            x1 = -rnd.nextInt(10);
            x2 = range + rnd.nextInt(10);
            y2 = y1 + rnd.nextInt(range / 10 + 1);
        } else if (kind < 0.95) { // otoceny interval (dolna hranica > horna)
            x2 = x1 - 1 - rnd.nextInt(5);
            y2 = y1 + rnd.nextInt(5);
        } else { // cela oblast
            x1 = 0;
            y1 = 0;
            x2 = range;
            y2 = range;
        }
        return new int[] { x1, y1, x2, y2 };
    }

    /**
     * Nahodne operacie: s pravdepodobnostou pInsert Insert, inak Find na nahodnom
     * intervale. pInsert = 0 testuje prazdny strom, vysoke pInsert zaplneny strom.
     */
    static void randomScenario(int count, double pInsert, int range, double dup, long seed) {
        newTree(range);
        Random rnd = new Random(seed);
        int finds = 0;
        verifyEvery = verifyEveryOption; // iba pocas nahodnych operacii
        try {
            for (int i = 0; i < count; i++) {
                if (rnd.nextDouble() < pInsert) {
                    int[] c = randomPoint(rnd, range, dup);
                    add(c[0], c[1]);
                } else {
                    int[] iv = randomInterval(rnd, range);
                    find(iv[0], iv[1], iv[2], iv[3], "operacia #" + i);
                    finds++;
                }
            }
        } finally {
            verifyEvery = 1; // zaverecna kontrola a dalsie testy overuju vzdy vsetko
        }
        for (int i = 0; i < 300; i++) { // zaverecna kontrola
            int[] iv = randomInterval(rnd, range);
            find(iv[0], iv[1], iv[2], iv[3], "zaverecna kontrola #" + i);
        }
        findAll(range, "zaverecna kontrola");
        System.out.println("      (vlozenych " + ref.size() + " bodov, " + (finds + 301) + " Find operacii)");
    }

    /**
     * Na zaplnenom strome spusti male intervaly: overi spravnost a ze priemerny
     * pocet porovnani na jeden Find je mensi ako n/4 (uplne prehladanie by
     * vyzadovalo aspon n porovnani), teda ze sa vyuziva priestorove delenie.
     */
    static void efficiencyTest(int n, int range, long seed) {
        newTree(range);
        Random rnd = new Random(seed);
        for (int i = 0; i < n; i++) {
            add(rnd.nextInt(range + 1), rnd.nextInt(range + 1));
        }
        int side = Math.max(1, range / 200);
        long total = 0;
        for (int i = 0; i < 300; i++) {
            int x = rnd.nextInt(range + 1 - side);
            int y = rnd.nextInt(range + 1 - side);
            total += find(x, y, x + side, y + side, "efektivita #" + i);
        }
        double average = total / 300.0;
        System.out.printf(Locale.ROOT, "      (n = %d, priemer porovnani na Find: %.1f, uplne prehladanie >= %d)%n",
                n, average, n);
        if (n >= 10000 && average >= n / 4.0) {
            throw new AssertionError("Find nevyuziva priestorove delenie: priemer porovnani " + average
                    + " >= n/4 = " + n / 4.0);
        }
    }

    // ----------------------------------------------------------------- testy

    static void run(String name, Runnable test) {
        long start = System.nanoTime();
        try {
            test.run();
            passed++;
            System.out.printf(Locale.ROOT, "  [PASS] %s (%d ms)%n", name, (System.nanoTime() - start) / 1_000_000);
        } catch (AssertionError e) {
            failed++;
            System.out.println("  [FAIL] " + name + ": " + e.getMessage());
        } catch (Exception | StackOverflowError e) {
            failed++;
            System.out.println("  [FAIL] " + name + ": neocakavana vynimka " + e);
        }
    }

    static void testStructure(String title) {
        final int R = 1000; // rozsah jednotkovych testov
        System.out.println("\n=== " + title + " ===\n-- jednotkove testy --");

        run("prazdny strom", () -> {
            newTree(R);
            find(0, 0, R, R, "prazdny strom");
            find(5, 5, 5, 5, "prazdny strom, bodovy interval");
        });
        run("jeden bod", () -> {
            newTree(R);
            add(100, 200);
            find(0, 0, R, R, "jeden bod");
            find(100, 200, 100, 200, "presny bodovy interval");
            find(101, 200, 300, 300, "interval hned vedla (x)");
            find(0, 0, 100, 199, "interval hned pod (y)");
            find(0, 0, 99, 300, "interval hned vlavo");
        });
        run("hranicne hodnoty (hranice vratane)", () -> {
            newTree(R);
            int[][] pts = { { 100, 100 }, { 200, 200 }, { 100, 200 }, { 200, 100 }, { 99, 150 }, { 201, 150 },
                    { 150, 99 },
                    { 150, 201 }, { 150, 150 }, { 99, 99 }, { 201, 201 }, { 100, 150 }, { 200, 150 }, { 150, 100 },
                    { 150, 200 } };
            for (int[] p : pts) {
                add(p[0], p[1]);
            }
            find(100, 100, 200, 200, "hranice");
            find(100, 100, 100, 100, "bodovy roh");
            find(100, 0, 100, R, "zvisla ciara x=100");
            find(0, 200, R, 200, "vodorovna ciara y=200");
            find(101, 101, 199, 199, "vnutro bez hranice");
        });
        run("rohy rozsahu", () -> {
            newTree(R);
            add(0, 0);
            add(R, R);
            add(0, R);
            add(R, 0);
            add(R / 2, R / 2);
            findAll(R, "rohy rozsahu");
            find(0, 0, 0, 0, "roh (0,0)");
            find(R, R, R, R, "roh (max,max)");
            find(0, R, 0, R, "roh (0,max)");
            find(R, 0, R, 0, "roh (max,0)");
            find(-50, -50, R + 50, R + 50, "interval vacsi ako rozsah");
        });
        run("duplicitne body (rozne objekty, rovnake kluce)", () -> {
            newTree(R);
            for (int i = 0; i < 7; i++) {
                add(300, 400);
            }
            add(300, 401);
            add(301, 400);
            add(300, 399);
            add(299, 400);
            find(300, 400, 300, 400, "bodovy interval");
            find(299, 399, 301, 401, "okolie");
            find(300, 0, 300, R, "ciara x=300");
            findAll(R, "duplicity");
            for (int i = 0; i < 4; i++) { // duplicity az po inych bodoch, aj v rohoch rozsahu
                add(0, 0);
                add(R, R);
            }
            find(0, 0, 0, 0, "duplicity v rohu (0,0)");
            find(R, R, R, R, "duplicity v rohu (max,max)");
            findAll(R, "duplicity");
        });
        run("prazdne, otocene a presahujuce intervaly", () -> {
            newTree(R);
            for (int i = 0; i < 200; i++) {
                add((i * 37) % (R + 1), (i * 91) % (R + 1));
            }
            find(500, 500, 400, 600, "otoceny (x1 > x2)");
            find(400, 600, 600, 500, "otoceny (y1 > y2)");
            find(-100, -100, -1, -1, "pod rozsahom");
            find(R + 1, R + 1, R + 50, R + 50, "nad rozsahom");
            find(-100, 0, R + 100, R / 2, "presahujuci rozsah");
        });
        run("kolinearne body (rovnaka hodnota v jednej dimenzii)", () -> {
            newTree(R);
            for (int i = 0; i <= 300; i++) {
                add(500, i * 3);
                add(i * 3, 500);
            }
            find(500, 0, 500, R, "x=500");
            find(0, 500, R, 500, "y=500");
            find(490, 490, 510, 510, "kriz");
            find(500, 100, 500, 200, "usek");
            findAll(R, "kolinearne");
        });
        run("zotriedeny vstup (nevyvazeny strom)", () -> {
            newTree(R);
            for (int i = 0; i < 3000; i++) {
                add(i % (R + 1), i % (R + 1));
            }
            Random rnd = new Random(7);
            for (int i = 0; i < 100; i++) {
                int x = rnd.nextInt(R + 1);
                int y = rnd.nextInt(R + 1);
                find(x, y, x + rnd.nextInt(300), y + rnd.nextInt(300), "zotriedeny vstup");
            }
            findAll(R, "zotriedeny vstup");
        });
        run("Find po kazdom Insert", () -> {
            newTree(R);
            Random rnd = new Random(11);
            for (int i = 0; i < 400; i++) {
                Point2D p = add(rnd.nextInt(60), rnd.nextInt(60));
                find(p.x, p.y, p.x, p.y, "po vlozeni (bodovy interval)");
                find(p.x - 5, p.y - 5, p.x + 5, p.y + 5, "po vlozeni (okolie)");
            }
            findAll(R, "priebezne vkladanie");
        });

        System.out.println("-- nahodne testy (" + ops + " operacii) --");
        if (pInsertOption >= 0) {
            run("nahodne operacie, P(Insert) = " + pInsertOption + ", range = " + rangeOption,
                    () -> randomScenario(ops, pInsertOption, rangeOption, pDup, seed));
        } else {
            run("prazdny strom: iba Find (P(Insert) = 0.0)",
                    () -> randomScenario(Math.min(ops, 20_000), 0.0, rangeOption, pDup, seed));
            run("zaciatok prazdny, vela Find (P(Insert) = 0.1)",
                    () -> randomScenario(ops, 0.1, rangeOption, pDup, seed + 1));
            run("vyvazene Insert/Find (P(Insert) = 0.5)",
                    () -> randomScenario(ops, 0.5, rangeOption, pDup, seed + 2));
            run("zaplnovanie stromu (P(Insert) = 0.9)",
                    () -> randomScenario(ops, 0.9, rangeOption, pDup, seed + 3));
            run("iba Insert + zaverecna kontrola (P(Insert) = 1.0)",
                    () -> randomScenario(ops, 1.0, rangeOption, pDup, seed + 4));
            run("husta oblast s vela duplicitami (range = 30, P(Insert) = 0.5)",
                    () -> randomScenario(Math.max(1, ops / 4), 0.5, 30, 0.3, seed + 5));
        }

        System.out.println("-- efektivita intervaloveho vyhladavania --");
        run("Find vyuziva priestorove delenie (nie uplne prehladanie)",
                () -> efficiencyTest(Math.max(ops, 10_000), rangeOption, seed + 6));
    }

    static void printUsage() {
        System.out.println("Pouzitie: java TreeTester [moznosti]");
        System.out.println("  --structure kd|quad|all  testovana struktura (predvolene all)");
        System.out.println("  --ops N                  pocet nahodnych operacii na scenar (predvolene 100000)");
        System.out.println("  --pInsert P              pravdepodobnost Insert (0..1), zvysok je Find;");
        System.out.println("                           bez neho bezia scenare 0, 0.1, 0.5, 0.9 a 1.0");
        System.out.println("  --range R                suradnice v [0, R] (predvolene 1000000)");
        System.out.println("  --pDup P                 pravdepodobnost duplicitneho bodu (predvolene 0.05)");
        System.out.println("  --maxDepth D             maximalna vyska Quad stromu (predvolene 20)");
        System.out.println(
                "  --verifyEvery N          v nahodnych operaciach overit iba kazdy N-ty Find (N >= 1, predvolene 1)");
        System.out.println("  --seed S                 seed generatora (predvolene 12345)");
    }

    public static void main(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--structure" -> structure = args[++i].toLowerCase(Locale.ROOT);
                    case "--ops" -> ops = Integer.parseInt(args[++i]);
                    case "--pInsert" -> pInsertOption = Double.parseDouble(args[++i]);
                    case "--range" -> rangeOption = Integer.parseInt(args[++i]);
                    case "--pDup" -> pDup = Double.parseDouble(args[++i]);
                    case "--maxDepth" -> maxDepth = Integer.parseInt(args[++i]);
                    case "--verifyEvery" -> verifyEveryOption = Integer.parseInt(args[++i]);
                    case "--seed" -> seed = Long.parseLong(args[++i]);
                    default -> throw new IllegalArgumentException("neznama moznost " + args[i]);
                }
            }
            if (ops < 1 || rangeOption < 10 || maxDepth < 0 || pDup < 0 || pDup > 1 || pInsertOption > 1
                    || verifyEveryOption < 1
                    || !(structure.equals("kd") || structure.equals("quad") || structure.equals("all"))) {
                throw new IllegalArgumentException("neplatne hodnoty argumentov");
            }
        } catch (RuntimeException e) {
            System.out.println("Chyba: " + e.getMessage());
            printUsage();
            return;
        }

        long start = System.nanoTime();
        System.out.println("TreeTester: structure=" + structure + ", ops=" + ops + ", range=" + rangeOption
                + ", seed=" + seed);
        if (!structure.equals("quad")) {
            useKd = true;
            testStructure("K-D strom");
        }
        if (!structure.equals("kd")) {
            useKd = false;
            testStructure("Quad strom (maxDepth = " + maxDepth + ")");
        }
        System.out.printf(Locale.ROOT, "%nVysledok: %d uspesnych, %d neuspesnych (%.1f s)%n", passed, failed,
                (System.nanoTime() - start) / 1e9);
        System.exit(failed == 0 ? 0 : 1);
    }
}
