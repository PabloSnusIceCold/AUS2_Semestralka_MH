// Trieda TreeTester.java bola vygenerovaná pomocou AI (Claude Sonnet 5.5)

import adaptery.*;
import model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * CLI tester priestorovych indexov implementujucich rozhranie SpatialIndex
 * (operacie Insert a Find).
 *
 * Podporuje synteticke testy aj benchmark na realnych datach (--data).
 *
 * Pouzitie: java TreeTester [--structure <kluc>|all] [--ops N]
 * [--pInsert P] [--range R] [--pDup P] [--maxDepth D] [--verifyEvery N] [--seed
 * S]
 * [--data] [--order file|shuffled] [--runs N] [--warmup N] [--verifyAreas N]
 */
public class TreeTester {
    // nastavenia z prikazoveho riadku
    static String structure = "all"; // testovana struktura: kluc z FACTORIES alebo "all"
    static int ops = 100_000; // pocet nahodnych operacii (Insert/Find)
    static double pInsertOption = -1; // -1 = prednastavene scenare
    static int rangeOption = 1000; // velkost suradnicove priestoru
    static double pDup = 0.05; // pravdepodobnost ze sa vlozi duplikat
    static int maxDepth = 20; // maximalna hlbka Quad stromu
    static long seed = 12345; // seed pre nahodne cisla
    static int verifyEveryOption = 1000; // --verifyEvery: overuje sa kazdy N-ty Find v nahodnych operaciach

    // rezim --data pre realne data
    static boolean dataMode = true;
    static String order = "file"; // "file" alebo "shuffled"
    static int runs = 1;
    static int warmup = 0;
    static int verifyAreas = 1000;

    static volatile long blackholeSink = 0;

    // overovanie Find: kazdy verifyEvery-ty Find sa porovna s referenciou
    static int verifyEvery = 1;
    static int findCounter = 0;

    // aktualne testovany strom a referencne zaznamy
    static String current; // prave testovana struktura
    static SpatialIndex index;

    // Sem sa pridavaju stiahnute implementacie
    static final List<IndexEntry> FACTORIES = new ArrayList<>();
    static {
        // Vlastne implementacie
        FACTORIES.add(new IndexEntry("kd", (x1, y1, x2, y2) -> new OwnKDTreeIndex()));
        FACTORIES.add(new IndexEntry("quad", (x1, y1, x2, y2) -> new OwnQuadTreeIndex(x1, y1, x2, y2, maxDepth)));
        FACTORIES.add(new IndexEntry("quadkd", (x1, y1, x2, y2) -> new OwnQuadKDTreeIndex(x1, y1, x2, y2, maxDepth)));
        // Externe KD-stromy
        FACTORIES.add(new IndexEntry("geoassist", (x1, y1, x2, y2) -> new GeoAssistKDTreeIndex()));
        FACTORIES.add(new IndexEntry("levy", (x1, y1, x2, y2) -> new LevyKDTreeIndex()));
        FACTORIES.add(new IndexEntry("tinspin", (x1, y1, x2, y2) -> new TinspinKDTreeIndex()));
        // Externe Quad-stromy
        FACTORIES.add(new IndexEntry("varunpant", (x1, y1, x2, y2) -> new VarunpantQuadTreeIndex(x1, y1, x2, y2)));
        FACTORIES.add(new IndexEntry("tree", (x1, y1, x2, y2) -> new TreeQuadTreeIndex(x1, y1, x2, y2)));
        FACTORIES.add(new IndexEntry("danysk", (x1, y1, x2, y2) -> new DanySKQuadTreeIndex()));
    }

    static IndexEntry findEntry(String key) {
        for (IndexEntry entry : FACTORIES) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    static ArrayList<GpsPoint> ref = new ArrayList<>();
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
        GpsComparator.counting = true;
        index = findEntry(current).getFactory().create(0, 0, range, range);
    }

    /**
     * Vlozi novy bod do stromu aj do referencneho zoznamu (id = pozicia v zozname).
     */
    static GpsPoint add(int x, int y) {
        GpsPoint p = new GpsPoint(ref.size(), x, y);
        ref.add(p);
        index.insert(p);
        return p;
    }

    /**
     * Spusti Find na strome a porovna ho s referenciou: musia sa zhodovat
     * konkretne objekty, ziadny nesmie chybat, byt navyse ani sa opakovat.
     *
     * @return pocet porovnani vykonanych stromom pocas Find
     */
    static long find(int x1, int y1, int x2, int y2, String ctx) {
        GpsPoint lower = new GpsPoint(-1, x1, y1);
        GpsPoint upper = new GpsPoint(-1, x2, y2);
        GpsComparator.calls = 0;
        List<GpsPoint> result = index.find(lower, upper);
        long used = index.comparisons();
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
        for (GpsPoint p : ref) {
            int px = (int) p.getX();
            int py = (int) p.getY();
            if (px >= x1 && px <= x2 && py >= y1 && py <= y2) {
                mark[(int) p.getId()] = stamp; // stav "ocakavany" - bod ma byt vo vysledku a strom ho este nevratil
                expected++;
            }
        }
        if (result.size() != expected) {
            throw new AssertionError(where + "pocet " + result.size() + " != ocakavany " + expected);
        }
        for (GpsPoint e : result) {
            if (e == null || e.getId() < 0 || e.getId() >= ref.size() || ref.get((int) e.getId()) != e) {
                throw new AssertionError(where + "vrateny objekt nebol vlozeny: " + e);
            }
            int id = (int) e.getId();
            if (mark[id] != stamp) { // bod nema byt vo vysledku (nie je v intervale)
                throw new AssertionError(where + "objekt " + e + " je mimo intervalu alebo je vrateny viackrat");
            }
            mark[id] = stamp + 1; // stav "vrateny" - bod ma byt vo vysledku a strom ho uz vratil
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
            GpsPoint p = ref.get(rnd.nextInt(ref.size()));
            return new int[] { (int) p.getX(), (int) p.getY() };
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
            GpsPoint p = ref.get(rnd.nextInt(ref.size()));
            x1 = x2 = (int) p.getX();
            y1 = y2 = (int) p.getY();
        } else if (kind < 0.84 && !ref.isEmpty()) { // hranice presne na suradniciach bodov
            GpsPoint p = ref.get(rnd.nextInt(ref.size()));
            GpsPoint q = ref.get(rnd.nextInt(ref.size()));
            int px = (int) p.getX();
            int py = (int) p.getY();
            int qx = (int) q.getX();
            int qy = (int) q.getY();
            x1 = Math.min(px, qx);
            y1 = Math.min(py, qy);
            x2 = Math.min(Math.max(px, qx), x1 + range / 4);
            y2 = Math.min(Math.max(py, qy), y1 + range / 4);
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
        if (index.comparisons() < 0) {
            System.out.println("      preskocene");
            return;
        }
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
                GpsPoint p = add(rnd.nextInt(60), rnd.nextInt(60));
                int px = (int) p.getX();
                int py = (int) p.getY();
                find(px, py, px, py, "po vlozeni (bodovy interval)");
                find(px - 5, py - 5, px + 5, py + 5, "po vlozeni (okolie)");
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
        StringBuilder keys = new StringBuilder();
        for (IndexEntry entry : FACTORIES) {
            if (keys.length() > 0) {
                keys.append("|");
            }
            keys.append(entry.getKey());
        }
        keys.append("|all");
        System.out.println("Pouzitie: java TreeTester [moznosti]");
        System.out.println("  --structure " + keys + "  testovana struktura (predvolene all)");
        System.out.println("  --ops N                  pocet nahodnych operacii na scenar (predvolene 100000)");
        System.out.println("  --pInsert P              pravdepodobnost Insert (0..1), zvysok je Find;");
        System.out.println("                           bez neho bezia scenare 0, 0.1, 0.5, 0.9 a 1.0");
        System.out.println("  --range R                suradnice v [0, R] (predvolene 1000000)");
        System.out.println("  --pDup P                 pravdepodobnost duplicitneho bodu (predvolene 0.05)");
        System.out.println("  --maxDepth D             maximalna vyska Quad stromu (predvolene 20)");
        System.out.println(
                "  --verifyEvery N          v nahodnych operaciach overit iba kazdy N-ty Find (N >= 1, predvolene 1)");
        System.out.println("  --seed S                 seed generatora (predvolene 12345)");
        System.out.println("  --data                   spusti benchmark na realnych datach");
        System.out.println("  --order file|shuffled    poradie vkladania bodov (predvolene file)");
        System.out.println("  --runs N                 pocet meranych behov benchmarku (N >= 1, predvolene 5)");
        System.out.println("  --warmup N               pocet zahrievacich behov (N >= 0, predvolene 2)");
        System.out.println(
                "  --verifyAreas N          pocet overovanych oblasti voci referencii (N >= 0, predvolene 1000)");
    }

    // ----------------------------------------------------------- rezim --data

    static class BenchmarkResult {
        String key;
        String name;
        boolean failed;
        String error;
        double insertMs;
        double findMs;
        double usPerArea;
        double smallMs;
        double mediumMs;
        double largeMs;
        double avgComparisons = -1;
        String boundaryStatus = "OK";
        String dupStatus = "Zachovava";
        int refErrors;
        int firstStructErrors;
        int[] counts;
        long[] idSums;
    }

    static void checkEdgeCases(IndexEntry entry, BenchmarkResult br) {
        try {
            SpatialIndex tree = entry.getFactory().create(0, 0, 100, 100);
            GpsPoint pInside = new GpsPoint(1, 15, 15);
            GpsPoint pCorner1 = new GpsPoint(2, 10, 10);
            GpsPoint pCorner2 = new GpsPoint(3, 20, 10);
            GpsPoint pCorner3 = new GpsPoint(4, 10, 20);
            GpsPoint pCorner4 = new GpsPoint(5, 20, 20);
            GpsPoint pMinX = new GpsPoint(6, 10, 15);
            GpsPoint pMaxX = new GpsPoint(7, 20, 15);
            GpsPoint pMinY = new GpsPoint(8, 15, 10);
            GpsPoint pMaxY = new GpsPoint(9, 15, 20);
            GpsPoint pOutL = new GpsPoint(10, 9.999, 15);
            GpsPoint pOutR = new GpsPoint(11, 20.001, 15);
            GpsPoint pOutB = new GpsPoint(12, 15, 9.999);
            GpsPoint pOutT = new GpsPoint(13, 15, 20.001);

            GpsPoint[] testPoints = { pInside, pCorner1, pCorner2, pCorner3, pCorner4,
                    pMinX, pMaxX, pMinY, pMaxY, pOutL, pOutR, pOutB, pOutT };
            for (GpsPoint p : testPoints) {
                tree.insert(p);
            }

            List<GpsPoint> resBoundary = tree.find(new GpsPoint(-1, 10, 10), new GpsPoint(-1, 20, 20));
            List<GpsPoint> resEmpty = tree.find(new GpsPoint(-1, 50, 50), new GpsPoint(-1, 60, 60));
            List<GpsPoint> resOutside = tree.find(new GpsPoint(-1, -50, -50), new GpsPoint(-1, -10, -10));

            if (resBoundary.size() == 9 && resEmpty.isEmpty() && resOutside.isEmpty()) {
                br.boundaryStatus = "OK";
            } else {
                br.boundaryStatus = resBoundary.size() + "/9";
            }

            SpatialIndex dupTree = entry.getFactory().create(0, 0, 100, 100);
            dupTree.insert(new GpsPoint(101, 30.5, 30.5));
            dupTree.insert(new GpsPoint(102, 30.5, 30.5));
            List<GpsPoint> resDup = dupTree.find(new GpsPoint(-1, 30, 30), new GpsPoint(-1, 31, 31));
            if (resDup.size() == 2) {
                br.dupStatus = "Zachovava";
            } else {
                br.dupStatus = "Obmedzenie (" + resDup.size() + "/2)";
            }
        } catch (Exception e) {
            br.boundaryStatus = "CHYBA";
        }
    }

    static double median(long[] arr) {
        if (arr.length == 0) {
            return 0.0;
        }
        long[] copy = arr.clone();
        Arrays.sort(copy);
        int mid = copy.length / 2;
        if (copy.length % 2 == 1) {
            return copy[mid] / 1e6;
        } else {
            return (copy[mid - 1] + copy[mid]) / 2.0 / 1e6;
        }
    }

    static void runDataBenchmark() {
        System.out.println("Nacitavam body a oblasti...");
        List<GpsPoint> points = DataLoader.loadNodes();
        List<Area> areas = DataLoader.loadAreas();
        if (order.equals("shuffled")) {
            Collections.shuffle(points, new Random(seed));
        }
        System.out.printf(Locale.ROOT, "Nacitanych %d bodov a %d oblasti (poradie: %s).%n",
                points.size(), areas.size(), order);

        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.size(); i++) {
            GpsPoint p = points.get(i);
            if (p.getX() < minX)
                minX = p.getX();
            if (p.getY() < minY)
                minY = p.getY();
            if (p.getX() > maxX)
                maxX = p.getX();
            if (p.getY() > maxY)
                maxY = p.getY();
        }

        int areaCount = areas.size();
        GpsPoint[] lowers = new GpsPoint[areaCount];
        GpsPoint[] uppers = new GpsPoint[areaCount];
        for (int i = 0; i < areaCount; i++) {
            Area a = areas.get(i);
            lowers[i] = new GpsPoint(-1, a.getMinX(), a.getMinY());
            uppers[i] = new GpsPoint(-1, a.getMaxX(), a.getMaxY());
        }

        int[] cat = new int[areaCount];
        double[] areaSizes = new double[areaCount];
        Integer[] orderIdx = new Integer[areaCount];
        for (int i = 0; i < areaCount; i++) {
            Area a = areas.get(i);
            areaSizes[i] = (a.getMaxX() - a.getMinX()) * (a.getMaxY() - a.getMinY());
            orderIdx[i] = i;
        }
        Arrays.sort(orderIdx, (i1, i2) -> Double.compare(areaSizes[i1], areaSizes[i2]));
        int third = areaCount / 3;
        for (int rank = 0; rank < areaCount; rank++) {
            int orig = orderIdx[rank];
            if (rank < third) {
                cat[orig] = 0; // male
            } else if (rank < 2 * third) {
                cat[orig] = 1; // stredne
            } else {
                cat[orig] = 2; // velke
            }
        }

        int verifyCount = (verifyAreas > 0) ? Math.min(verifyAreas, areaCount) : 0;
        int[] refAreaIndices = new int[verifyCount];
        int[] refCounts = new int[verifyCount];
        long[] refIdSums = new long[verifyCount];

        if (verifyCount > 0) {
            int step = Math.max(1, areaCount / verifyCount);
            int pSize = points.size();
            double[] xs = new double[pSize];
            double[] ys = new double[pSize];
            long[] ids = new long[pSize];
            for (int p = 0; p < pSize; p++) {
                GpsPoint pt = points.get(p);
                xs[p] = pt.getX();
                ys[p] = pt.getY();
                ids[p] = pt.getId();
            }
            System.out.printf(Locale.ROOT, "Pocitam referenciu pre %d overovanych oblasti (krok = %d)...%n",
                    verifyCount, step);
            long refT0 = System.nanoTime();
            for (int v = 0; v < verifyCount; v++) {
                int aIdx = v * step;
                if (aIdx >= areaCount)
                    aIdx = areaCount - 1;
                refAreaIndices[v] = aIdx;
                Area a = areas.get(aIdx);
                double ax1 = a.getMinX();
                double ay1 = a.getMinY();
                double ax2 = a.getMaxX();
                double ay2 = a.getMaxY();
                int cnt = 0;
                long sum = 0;
                for (int p = 0; p < pSize; p++) {
                    double px = xs[p];
                    double py = ys[p];
                    if (px >= ax1 && px <= ax2 && py >= ay1 && py <= ay2) {
                        cnt++;
                        sum += ids[p];
                    }
                }
                refCounts[v] = cnt;
                refIdSums[v] = sum;
            }
            System.out.printf(Locale.ROOT, "Referencia dopocitana (%.2f s).%n", (System.nanoTime() - refT0) / 1e9);
        }

        List<IndexEntry> entriesToTest = new ArrayList<>();
        for (IndexEntry entry : FACTORIES) {
            if (structure.equals("all") || structure.equals(entry.getKey())) {
                entriesToTest.add(entry);
            }
        }

        List<BenchmarkResult> results = new ArrayList<>();
        for (IndexEntry entry : entriesToTest) {
            BenchmarkResult br = new BenchmarkResult();
            br.key = entry.getKey();
            br.name = entry.getKey();
            checkEdgeCases(entry, br);
            System.out.println("\n--- Benchmark: " + entry.getKey() + " ---");
            try {
                long[] insertTimes = new long[runs];
                long[] smallTimes = new long[runs];
                long[] mediumTimes = new long[runs];
                long[] largeTimes = new long[runs];

                SpatialIndex lastTree = null;
                int totalRuns = warmup + runs;
                int pSize = points.size();

                for (int r = 0; r < totalRuns; r++) {
                    boolean isWarmup = (r < warmup);
                    System.gc();
                    GpsComparator.counting = false;
                    SpatialIndex tree = entry.getFactory().create(minX, minY, maxX, maxY);
                    if (r == 0) {
                        br.name = tree.name();
                    }

                    long t0 = System.nanoTime();
                    for (int p = 0; p < pSize; p++) {
                        tree.insert(points.get(p));
                    }
                    long insDur = System.nanoTime() - t0;

                    long timeSmall = 0;
                    long timeMedium = 0;
                    long timeLarge = 0;
                    for (int i = 0; i < areaCount; i++) {
                        long f0 = System.nanoTime();
                        List<GpsPoint> res = tree.find(lowers[i], uppers[i]);
                        long fDur = System.nanoTime() - f0;
                        blackholeSink += res.size();
                        int c = cat[i];
                        if (c == 0)
                            timeSmall += fDur;
                        else if (c == 1)
                            timeMedium += fDur;
                        else
                            timeLarge += fDur;
                    }

                    if (!isWarmup) {
                        int mIdx = r - warmup;
                        insertTimes[mIdx] = insDur;
                        smallTimes[mIdx] = timeSmall;
                        mediumTimes[mIdx] = timeMedium;
                        largeTimes[mIdx] = timeLarge;
                    }
                    lastTree = tree;
                }

                br.insertMs = median(insertTimes);
                br.smallMs = median(smallTimes);
                br.mediumMs = median(mediumTimes);
                br.largeMs = median(largeTimes);
                br.findMs = br.smallMs + br.mediumMs + br.largeMs;
                br.usPerArea = (br.findMs * 1000.0) / areaCount;

                System.out.printf(Locale.ROOT,
                        "  vkladanie: %.1f ms, vyhladavanie: %.1f ms (male: %.1f ms, stredne: %.1f ms, velke: %.1f ms)%n",
                        br.insertMs, br.findMs, br.smallMs, br.mediumMs, br.largeMs);

                // d) overenie MIMO merania
                if (verifyCount > 0) {
                    System.out.print("  Overujem spravnost (" + verifyCount + " oblasti)...");
                    GpsComparator.counting = true;
                    br.counts = new int[verifyCount];
                    br.idSums = new long[verifyCount];
                    long totalComparisons = 0;
                    boolean canCount = (lastTree.comparisons() >= 0);

                    for (int v = 0; v < verifyCount; v++) {
                        int aIdx = refAreaIndices[v];
                        GpsComparator.calls = 0;
                        List<GpsPoint> res = lastTree.find(lowers[aIdx], uppers[aIdx]);
                        br.counts[v] = res.size();
                        long idSum = 0;
                        for (int k = 0; k < res.size(); k++) {
                            idSum += res.get(k).getId();
                        }
                        br.idSums[v] = idSum;
                        if (canCount) {
                            long c = lastTree.comparisons();
                            if (c >= 0) {
                                totalComparisons += c;
                            }
                        }
                    }
                    GpsComparator.counting = false;
                    br.avgComparisons = canCount ? ((double) totalComparisons / verifyCount) : -1.0;
                    System.out.println(" hotovo.");
                }

            } catch (Throwable t) {
                br.failed = true;
                br.error = t.getClass().getSimpleName() + (t.getMessage() != null ? (": " + t.getMessage()) : "");
                System.out.println("  [FAIL] " + entry.getKey() + ": " + br.error);
            }
            results.add(br);
        }

        // porovnanie
        BenchmarkResult firstSuccess = null;
        for (BenchmarkResult br : results) {
            if (br.failed)
                continue;

            if (verifyCount > 0) {
                int refDiff = 0;
                for (int v = 0; v < verifyCount; v++) {
                    if (br.counts[v] != refCounts[v] || br.idSums[v] != refIdSums[v]) {
                        refDiff++;
                    }
                }
                br.refErrors = refDiff;
            } else {
                br.refErrors = 0;
            }

            if (firstSuccess == null) {
                firstSuccess = br;
                br.firstStructErrors = 0;
            } else {
                int diffFirst = 0;
                for (int v = 0; v < verifyCount; v++) {
                    if (br.counts[v] != firstSuccess.counts[v] || br.idSums[v] != firstSuccess.idSums[v]) {
                        diffFirst++;
                    }
                }
                br.firstStructErrors = diffFirst;
            }
        }

        // tabulka
        System.out.println();
        System.out.println("=== Vysledky benchmarku (" + (order.equals("shuffled") ? "shuffled" : "file") + ", runs="
                + runs + ", warmup=" + warmup + ", verify=" + verifyCount + ") ===");
        String header = String.format(Locale.ROOT, "%-24s | %14s | %17s | %11s | %27s | %8s | %17s | %10s | %11s",
                "Nazov", "Vkladanie [ms]", "Vyhladavanie [ms]", "us / oblast", "Male / Stredne / Velke [ms]",
                "Hranice", "Duplicity", "Chyb ref", "Chyb 1. str");
        System.out.println(header);
        System.out.println("-".repeat(header.length()));
        for (BenchmarkResult br : results) {
            if (br.failed) {
                System.out.printf(Locale.ROOT, "%-24s | FAIL: %s%n", br.name, br.error);
            } else {
                String tripple = String.format(Locale.ROOT, "%.1f / %.1f / %.1f", br.smallMs, br.mediumMs, br.largeMs);
                String firstErrStr = (br == firstSuccess) ? "0" : String.valueOf(br.firstStructErrors);

                System.out.printf(Locale.ROOT, "%-24s | %14.1f | %17.1f | %11.2f | %27s | %8s | %17s | %10d | %11s%n",
                        br.name, br.insertMs, br.findMs, br.usPerArea, tripple, br.boundaryStatus, br.dupStatus,
                        br.refErrors, firstErrStr);
            }
        }
        System.out.println("-".repeat(header.length()));

        String reportFileName = "vysledky_" + java.time.LocalDate.now() + ".txt";
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.FileWriter(reportFileName, java.nio.charset.StandardCharsets.UTF_8))) {
            pw.println("=== BENCHMARK VYSLEDKY SPATIAL INDEXOV ===");
            pw.println("Datum a cas: " + java.time.LocalDateTime.now());
            pw.println("Konfiguracia: poradie=" + order + ", runs=" + runs + ", warmup=" + warmup + ", verifyAreas="
                    + verifyCount);
            pw.println("Vstupne data: " + points.size() + " bodov, " + areas.size() + " testovacich oblasti");
            pw.println();
            pw.println(header);
            pw.println("-".repeat(header.length()));
            for (BenchmarkResult br : results) {
                if (br.failed) {
                    pw.printf(Locale.ROOT, "%-24s | FAIL: %s%n", br.name, br.error);
                } else {
                    String tripple = String.format(Locale.ROOT, "%.1f / %.1f / %.1f", br.smallMs, br.mediumMs,
                            br.largeMs);
                    String firstErrStr = (br == firstSuccess) ? "0" : String.valueOf(br.firstStructErrors);
                    pw.printf(Locale.ROOT, "%-24s | %14.1f | %17.1f | %11.2f | %27s | %8s | %17s | %10d | %11s%n",
                            br.name, br.insertMs, br.findMs, br.usPerArea, tripple, br.boundaryStatus, br.dupStatus,
                            br.refErrors, firstErrStr);
                }
            }
            pw.println("-".repeat(header.length()));
            pw.println();
            pw.println("Poznamky a obmedzenia struktur:");
            pw.println(
                    "1. Vlastny KDTree / QuadTree / QuadKDTree: plna podpora duplicitnych suradnic, presne intervalove vyhladavanie vratane hranic.");
            pw.println(
                    "2. Tinspin KDTree: vysoko optimalizovany K-d strom, podporuje duplicity, presne intervalove vyhladavanie.");
            pw.println(
                    "3. Levy KDTree: nepodporuje duplicitne kluce (vyhadzuje KeyDuplicateException), preto duplicity vynechava; inak korektne vyhladava.");
            pw.println(
                    "4. Geo Assist KDTree: interne uchovava ID v mape a pri zhode suradnic preskakuje duplikaty; inak korektne intervalove vyhladavanie.");
            pw.println(
                    "5. Tree (Kirsty) QuadTree: region quadtree s listami obsahujucimi kolekciu hodnot, plna podpora duplicit.");
            pw.println(
                    "6. Varunpant QuadTree: point quadtree, pri vkladani bodu s rovnakymi suradnicami prepisuje predchadzajuci prvok (nepodporuje duplicitne suradnice).");
            pw.println(
                    "7. DanySK FlexibleQuadTree: flexibilny dynamicky rastuci quadtree v Kotline, plna integracia s kotlin-stdlib.");
            System.out.println("Vysledky boli ulozene do suboru: " + reportFileName);
        } catch (java.io.IOException ioe) {
            System.err.println("Nepodarilo sa ulozit vysledky do suboru: " + ioe.getMessage());
        }
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
                    case "--data" -> dataMode = true;
                    case "--order" -> order = args[++i].toLowerCase(Locale.ROOT);
                    case "--runs" -> runs = Integer.parseInt(args[++i]);
                    case "--warmup" -> warmup = Integer.parseInt(args[++i]);
                    case "--verifyAreas" -> verifyAreas = Integer.parseInt(args[++i]);
                    default -> throw new IllegalArgumentException("neznama moznost " + args[i]);
                }
            }
            if (ops < 1 || rangeOption < 10 || maxDepth < 0 || pDup < 0 || pDup > 1 || pInsertOption > 1
                    || verifyEveryOption < 1
                    || runs < 1 || warmup < 0 || verifyAreas < 0
                    || !(order.equals("file") || order.equals("shuffled"))
                    || !(structure.equals("all") || findEntry(structure) != null)) {
                throw new IllegalArgumentException("neplatne hodnoty argumentov");
            }
        } catch (RuntimeException e) {
            System.out.println("Chyba: " + e.getMessage());
            printUsage();
            return;
        }

        if (dataMode) {
            runDataBenchmark();
            return;
        }

        long start = System.nanoTime();
        System.out.println("TreeTester: structure=" + structure + ", ops=" + ops + ", range=" + rangeOption
                + ", seed=" + seed);
        for (IndexEntry entry : FACTORIES) {
            String key = entry.getKey();
            if (structure.equals("all") || structure.equals(key)) {
                current = key;
                index = entry.getFactory().create(0, 0, rangeOption, rangeOption);
                testStructure(index.name());
            }
        }
        System.out.printf(Locale.ROOT, "%nVysledok: %d uspesnych, %d neuspesnych (%.1f s)%n", passed, failed,
                (System.nanoTime() - start) / 1e9);
        System.exit(failed == 0 ? 0 : 1);
    }
}
