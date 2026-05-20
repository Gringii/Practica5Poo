import java.io.*;
import java.util.*;

/**
 * Clase Dictionary
 * 
 * Almacena las palabras del diccionario usando un HashMap<String, WordMetadata>
 * donde la llave es la palabra y el valor contiene metadatos (definición y longitud).
 * 
 * USO DE HashMap: Se eligió HashMap porque permite búsqueda O(1) para verificar
 * si una palabra existe en el diccionario, lo cual es crítico para la validación
 * rápida de cada intento del jugador.
 * 
 * USO DE Iterator: Se usa Iterator para recorrer las palabras del diccionario
 * y encontrar la palabra secreta aleatoria, así como para calcular distancias.
 */
public class Dictionary {

    // === HashMap: llave = palabra, valor = metadatos ===
    private HashMap<String, WordMetadata> words;
    private List<String> sortedWords; // Lista ordenada para búsqueda binaria y distancias
    private String language;

    public Dictionary(String language) {
        this.language = language;
        this.words = new HashMap<>();
        this.sortedWords = new ArrayList<>();
    }

    /**
     * Carga palabras desde archivo de recursos.
     * Las palabras se filtran por longitud según dificultad.
     */
    public void loadWords(int wordLength) {
        words.clear();
        sortedWords.clear();

        String filename = language.equals("es") ? "words_es.txt" : "words_en.txt";
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);

        if (is == null) {
            // Fallback: buscar en directorio resources relativo
            try {
                File f = new File("resources/" + filename);
                if (f.exists()) {
                    is = new FileInputStream(f);
                }
            } catch (Exception e) {
                // ignore
            }
        }

        if (is == null) {
            System.err.println("[AVISO] No se encontró el archivo de diccionario: " + filename);
            loadFallbackWords(wordLength);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if (word.length() == wordLength && word.matches("[a-záéíóúüñ]+")) {
                    WordMetadata meta = new WordMetadata(word.length(), "");
                    words.put(word, meta);
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Leyendo diccionario: " + e.getMessage());
        }

        // Construir lista ordenada
        // LAMBDA #1: Comparador usando lambda para ordenar palabras alfabéticamente
        sortedWords = new ArrayList<>(words.keySet());
        sortedWords.sort((a, b) -> a.compareTo(b));

        System.out.println("[INFO] Diccionario cargado: " + words.size() + " palabras de " + wordLength + " letras en " + language.toUpperCase());
    }

    /**
     * Palabras de respaldo en caso de no encontrar el archivo.
     */
    private void loadFallbackWords(int wordLength) {
        String[] fallbackEn5 = {
            "about","above","abuse","actor","acute","admit","adopt","adult","after","again",
            "agent","agree","ahead","alarm","alert","alike","alive","alley","allow","alone",
            "along","alter","angel","angle","angry","anime","ankle","annex","apple","apply",
            "arena","argue","arise","armor","aroma","array","arrow","asked","atlas","atone",
            "audio","audit","avail","avoid","awake","award","aware","awful","azure","badge",
            "baker","basic","basil","batch","bathe","beach","beard","beast","began","being",
            "belle","bench","bible","birch","birth","bison","black","blade","blame","blank",
            "blast","blaze","bleak","blend","block","blood","bloom","blown","blues","blunt",
            "board","bonus","bound","boxer","braid","brake","brand","brave","bread","break",
            "breed","brick","bride","bring","brisk","brook","broom","broth","brown","brush",
            "brute","buddy","bulge","bulky","bully","bunch","burst","candy","canon","caper",
            "carve","cause","cedar","chain","chair","chaos","charm","chasm","cheat","cheek",
            "chess","chest","chief","chill","choir","chord","chore","civic","civil","claim",
            "clamp","clash","clasp","clean","clear","clerk","cliff","climb","cling","cloak",
            "clock","clone","close","cloth","cloud","clout","clown","coast","comet","comic",
            "coral","couch","count","court","craft","crane","crash","crawl","creek","creep",
            "crest","crisp","cross","crowd","crown","crush","cycle","daisy","dance","dated",
            "dealt","depot","depth","devil","dirty","disco","dodge","donor","doubt","dough",
            "draft","drain","dream","dress","dried","drill","drink","drool","drove","drown",
            "drunk","dusty","dwarf","dwell","eager","eagle","early","earth","empty","enjoy",
            "equal","error","essay","evade","event","exact","exalt","extra","fable","faint",
            "fairy","faith","false","fancy","fatal","fault","feast","feral","fetch","fever",
            "fiend","fight","final","finch","first","fixed","flake","flame","fleet","flesh",
            "fling","float","flock","flood","floor","flour","flute","focal","folio","found",
            "frame","frank","fraud","fresh","front","frost","frown","fungi","funky","funny",
            "gauze","given","gland","glare","glass","gleam","glide","gloom","gloss","glove",
            "going","grace","grade","grain","grant","grasp","grate","graze","greed","grief",
            "grill","grind","groan","groom","gross","growl","grunt","guild","guile","gusto",
            "habit","haiku","halve","happy","harsh","hasty","hatch","haven","havoc","heart",
            "heavy","hedge","heist","hippo","hobby","holly","honor","hover","human","humor",
            "hunch","hyena","icing","ideal","image","inept","inert","intro","irate","irony",
            "ivory","judge","juice","juicy","jumbo","kayak","khaki","knack","knife","knock",
            "known","label","lance","large","laser","laugh","layer","leafy","leapt","learn",
            "lease","legal","lemon","level","light","lilac","linen","lodge","logic","lucky",
            "magic","maize","maker","manor","maple","march","marsh","match","mayor","medal",
            "mercy","merit","messy","metal","might","mimic","minor","mirth","mixed","model",
            "moist","money","monks","moody","moral","mourn","mouse","music","musty","naive",
            "nasty","naval","nerve","niece","night","ninja","noisy","nomad","north","notch",
            "novel","oasis","ocean","olive","order","organ","otter","panic","paper","pasta",
            "patch","pause","peach","perch","peril","petty","phase","phony","piano","pilot",
            "pixel","pizza","place","plaid","plain","plane","plank","plaza","plumb","plume",
            "plush","point","poker","polar","poppy","porch","power","prank","press","price",
            "pride","prime","print","prism","probe","proof","prose","prowl","prune","pulse",
            "punch","purge","quack","queen","query","quest","quick","quiet","quirk","quote",
            "rabbi","rabid","radar","rainy","rally","range","rapid","raven","reach","ready",
            "realm","regal","reign","relax","relay","resin","rider","ridge","rifle","right",
            "risky","rival","river","roast","robin","rocky","rogue","rouge","rough","round",
            "rover","rowdy","rugby","ruler","rumba","rusty","saint","salad","samba","sandy",
            "sassy","sauce","savvy","scene","scone","scope","score","scout","seize","sense",
            "shade","shaft","shaky","shame","shape","sharp","sheer","shelf","shell","shift",
            "shirt","shock","shoot","shore","short","shout","shove","siege","sieve","silly",
            "sixth","sixty","skate","skill","skirt","slack","slash","sleep","slide","sling",
            "sloth","small","smart","smear","smell","smile","smoke","snack","snail","snare",
            "sneak","solar","solid","sorry","south","space","spare","spark","speak","speed",
            "spend","spice","spill","spine","spite","split","spoon","sport","spray","squad",
            "squat","stack","staff","stage","stain","stale","stamp","stand","stark","start",
            "state","steak","steal","steam","steel","steep","steer","stick","sting","stomp",
            "stood","stool","store","storm","story","stout","stove","strap","straw","stray",
            "strip","stuck","study","stuff","stump","stung","stunt","sugar","suite","sunny",
            "super","surge","swamp","swear","sweat","sweep","sweet","swift","swipe","swirl",
            "tabby","taint","talon","tango","teach","tease","teeth","tense","thank","theme",
            "thick","thief","three","threw","throw","tiger","tight","timid","today","topic",
            "torch","total","touch","tough","tower","toxic","trace","track","trail","train",
            "trait","trash","trawl","tread","treat","trend","trial","tribe","trick","tried",
            "troop","trout","truck","truly","trunk","trust","truth","tulip","tuner","tweak",
            "ultra","unify","union","until","upper","upset","usher","usual","utter","vague",
            "valid","valor","value","vault","verge","verse","vigil","vinyl","viper","viral",
            "visit","vista","vivid","vocal","vodka","wacky","waltz","watch","water","weave",
            "wedge","weird","where","which","while","whirl","white","whole","whose","windy",
            "witch","world","worst","worth","would","wrath","write","wrong","yacht","yearn",
            "young","youth","zebra","zesty"
        };

        String[] fallbackEs5 = {
            "abeja","abono","abril","abuso","acero","acoso","aguja","ajeno","album","aldea",
            "aleta","almas","altos","amado","amiga","amigo","amor","angel","antes","anual",
            "arbol","ardor","arena","armar","armas","arroz","asado","asilo","astro","atomo",
            "audio","autor","avena","avion","aviso","ayuda","banca","banco","banda","barro",
            "barco","beber","bella","bello","besar","bicho","blusa","bolsa","bomba","bordo",
            "boton","bravo","brisa","brote","bueno","buque","burla","busca","cable","caida",
            "calma","calor","campo","canal","canoa","cargo","carpa","carta","casco","causa",
            "cavar","cazar","cebra","cedro","celda","cerdo","cerro","cesta","cielo","cifra",
            "cinco","cinta","circo","cisne","claro","clavo","clima","cocer","coche","color",
            "combo","copia","coral","corte","costa","creer","criba","cruce","crudo","cueva",
            "culpa","culto","cuota","curar","curva","datos","decir","dejar","delta","denso",
            "depot","derbi","deseo","deuda","dicha","dicho","disco","dolor","donar","donde",
            "dudar","duelo","dulce","dunas","ebrio","echar","enojo","error","fabrica","facil",
            "fango","fardo","feria","feroz","ficha","fideo","fiero","fijar","finca","firma",
            "flaco","flota","flujo","folio","fondo","freno","fresa","fruta","fuego","garra",
            "ganar","garza","genio","gesto","girar","globo","golfo","gordo","gorra","grado",
            "grano","grasa","grave","grupo","guapa","guapo","guiar","guion","gusto","habia",
            "hacia","hampa","helio","hiena","hielo","honra","hotel","hueso","huevo","huida",
            "humor","hurto","icono","ideal","idolo","igual","ileso","indio","inicio","labia",
            "labio","ladra","laico","lance","lapiz","largo","laser","laudo","lazo","leche",
            "legal","letra","ligar","limon","linea","listo","llano","logro","lucha","lunar",
            "lustre","madre","mafia","magia","mango","mania","manos","manso","manto","marea",
            "medir","mejor","menor","mente","miedo","mirar","mismo","mitad","mitra","mojar",
            "molde","monte","morir","morse","motor","mover","mucho","muela","mundo","musgo",
            "muslo","nacer","nadie","negar","negro","nieve","noble","noche","norma","norte",
            "novio","nuevo","obeso","obrar","obvio","odiar","oeste","oficio","olivo","olvido",
            "opera","orden","oruga","osado","ostra","oveja","padre","pagar","palco","panda",
            "pared","parir","paseo","pausa","pedal","pelea","penal","perro","pesar","picar",
            "pieza","pinza","pisar","pista","playa","plaza","plomo","poder","poema","polar",
            "polvo","poner","porta","primo","prisa","pronto","pulpo","punto","purga","queja",
            "queso","radio","razon","regio","reino","reloj","renta","resto","retro","rezar",
            "rigor","robot","ronda","rueda","rugir","ruido","rumbo","saber","sabor","salir",
            "salud","salvo","santo","savia","secar","sello","sexto","siglo","signo","sitio",
            "sobre","sonar","soplo","suave","subir","sucio","suelo","tarea","techo","temor",
            "tener","tesis","tibio","timon","tirar","torpe","trama","trigo","trino","truco",
            "tumor","turno","unido","union","urano","usual","vacuo","valor","vapor","vasto",
            "velar","venda","venir","venta","verde","verso","viaje","vigor","viral","virus",
            "vista","vivir","volar","votar","vuelo","yacer","yerno","yerro","yunta","zumo"
        };

        String[] fallback = language.equals("es") ? fallbackEs5 : fallbackEn5;

        for (String w : fallback) {
            if (w.length() == wordLength) {
                words.put(w, new WordMetadata(w.length(), ""));
            }
        }

        sortedWords = new ArrayList<>(words.keySet());
        sortedWords.sort((a, b) -> a.compareTo(b));

        System.out.println("[INFO] Diccionario de respaldo: " + words.size() + " palabras");
    }

    /**
     * Verifica si una palabra existe en el diccionario.
     * USO DE HashMap: búsqueda O(1) con containsKey.
     */
    public boolean contains(String word) {
        return words.containsKey(word.toLowerCase());
    }

    /**
     * Agrega una nueva palabra al diccionario (con definición del usuario).
     */
    public void addWord(String word, String definition) {
        word = word.toLowerCase();
        words.put(word, new WordMetadata(word.length(), definition));
        // Reinsertar en lista ordenada
        sortedWords.add(word);
        sortedWords.sort((a, b) -> a.compareTo(b));
    }

    /**
     * Obtiene una palabra secreta aleatoria del diccionario.
     * USO DE Iterator: recorremos el HashMap con iterator para contar y seleccionar.
     */
    public String getRandomWord() {
        if (words.isEmpty()) return null;
        int index = new Random().nextInt(words.size());
        // LAMBDA #2: uso de stream con lambda para obtener palabra aleatoria
        return words.keySet().stream()
                .sorted()
                .skip(index)
                .findFirst()
                .orElse(null);
    }

    /**
     * Calcula el índice de una palabra en la lista ordenada.
     */
    public int getIndex(String word) {
        return Collections.binarySearch(sortedWords, word.toLowerCase());
    }

    /**
     * Calcula el porcentaje de distancia entre dos palabras en el diccionario.
     * @return porcentaje de palabras entre word1 y word2 respecto al total
     */
    public double getDistancePercent(String word1, String word2) {
        int idx1 = getIndex(word1);
        int idx2 = getIndex(word2);
        if (idx1 < 0 || idx2 < 0) return -1;
        int distance = Math.abs(idx2 - idx1);
        return (distance * 100.0) / sortedWords.size();
    }

    /**
     * Determina si la palabra secreta está ANTES o DESPUÉS de la palabra ingresada.
     * @return -1 si secretWord está antes, 1 si está después
     */
    public int compareWords(String guessedWord, String secretWord) {
        return guessedWord.toLowerCase().compareTo(secretWord.toLowerCase());
    }

    /**
     * Obtiene la palabra que está N% por encima de la palabra dada.
     */
    public String getWordAboveByPercent(String word, double percent) {
        int idx = getIndex(word);
        if (idx < 0) return null;
        int steps = (int) (percent / 100.0 * sortedWords.size());
        int newIdx = Math.max(0, idx - steps);
        return sortedWords.get(newIdx);
    }

    /**
     * Obtiene la palabra que está N% por debajo de la palabra dada.
     */
    public String getWordBelowByPercent(String word, double percent) {
        int idx = getIndex(word);
        if (idx < 0) return null;
        int steps = (int) (percent / 100.0 * sortedWords.size());
        int newIdx = Math.min(sortedWords.size() - 1, idx + steps);
        return sortedWords.get(newIdx);
    }

    /**
     * Obtiene la primera letra de la palabra secreta (pista tipo c).
     */
    public char getFirstLetter(String secretWord) {
        return secretWord.charAt(0);
    }

    /**
     * USO DE Iterator: itera sobre todas las palabras del diccionario.
     * Útil para búsquedas y estadísticas.
     */
    public Iterator<Map.Entry<String, WordMetadata>> getIterator() {
        return words.entrySet().iterator();
    }

    public List<String> getSortedWords() {
        return Collections.unmodifiableList(sortedWords);
    }

    public int size() {
        return words.size();
    }

    public String getLanguage() {
        return language;
    }

    // =========================================================
    // Clase interna para metadatos de palabras
    // =========================================================
    public static class WordMetadata {
        private int length;
        private String definition;

        public WordMetadata(int length, String definition) {
            this.length = length;
            this.definition = definition;
        }

        public int getLength() { return length; }
        public String getDefinition() { return definition; }
        public void setDefinition(String def) { this.definition = def; }

        @Override
        public String toString() {
            return "WordMetadata{len=" + length + ", def='" + definition + "'}";
        }
    }
}
