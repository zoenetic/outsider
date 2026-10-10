// Generates GIMP/Aseprite .gpl palettes from vanilla Minecraft textures.
//
//     kotlin scripts/vanilla_palette.main.kts block/granite
//     kotlin scripts/vanilla_palette.main.kts "block/*_planks" item/stick
//     kotlin scripts/vanilla_palette.main.kts block/stone block/cobblestone --merge stones
//     kotlin scripts/vanilla_palette.main.kts block/granite --jar path/to/client.jar
//
// Texture names are paths under assets/minecraft/textures without ".png" and may use * and ?
// wildcards. With no --jar or --dir the client jar for the version in gradle.properties is
// downloaded once into scripts/.cache/.
//
// PNGs are decoded by hand (not ImageIO) so every colour is exactly what the file stores.

@file:Import("lib/common.main.kts")

import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.nio.ByteBuffer
import java.util.zip.Inflater
import java.util.zip.ZipFile
import kotlin.system.exitProcess

val ROOT: File = __FILE__.canonicalFile.parentFile.parentFile
val ART_DIR = ROOT.resolve("art")
val CACHE_DIR = ROOT.resolve("scripts/.cache")
val TEXTURE_PREFIX = "assets/minecraft/textures/"
val MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

fun fail(message: String): Nothing {
    System.err.println(message)
    exitProcess(1)
}

// --- texture sources ---------------------------------------------------------------------------

fun minecraftVersion(): String =
    ROOT.resolve("gradle.properties").readLines()
        .map { it.split("=", limit = 2) }
        .firstOrNull { it.size == 2 && it[0].trim() == "minecraft_version" }?.get(1)?.trim()
        ?: fail("minecraft_version not found in gradle.properties")

fun fetchJson(url: String): Map<String, Any?> = parseJson(URI(url).toURL().readText()).obj()

fun clientJar(version: String): File {
    val jar = CACHE_DIR.resolve("client-$version.jar")
    if (jar.exists()) return jar
    val entry = fetchJson(MANIFEST_URL).getValue("versions").list().map { it.obj() }
        .firstOrNull { it["id"] == version }
        ?: fail("Minecraft $version not in Mojang's version manifest; pass --jar")
    val url = fetchJson(entry["url"] as String).getValue("downloads").obj().getValue("client").obj()["url"] as String
    System.err.println("Downloading Minecraft $version client jar...")
    CACHE_DIR.mkdirs()
    val part = jar.resolveSibling(jar.name + ".part")
    URI(url).toURL().openStream().use { input -> part.outputStream().use { input.copyTo(it) } }
    part.renameTo(jar)
    return jar
}

interface Source {
    val names: List<String>
    fun read(name: String): ByteArray
}

class JarSource(path: File) : Source {
    private val zip = ZipFile(path)
    override val names = zip.entries().asSequence().map { it.name }
        .filter { it.startsWith(TEXTURE_PREFIX) && it.endsWith(".png") }
        .map { it.removePrefix(TEXTURE_PREFIX).removeSuffix(".png") }.toList()

    override fun read(name: String): ByteArray = zip.getInputStream(zip.getEntry("$TEXTURE_PREFIX$name.png")).readBytes()
}

class DirSource(private val root: File) : Source {
    override val names = root.walkTopDown().filter { it.isFile && it.extension == "png" }
        .map { it.relativeTo(root).invariantSeparatorsPath.removeSuffix(".png") }.toList()

    override fun read(name: String): ByteArray = root.resolve("$name.png").readBytes()
}

// --- PNG decoding ------------------------------------------------------------------------------

val CHANNELS = mapOf(0 to 1, 2 to 3, 3 to 1, 4 to 2, 6 to 4)

/** Every pixel as (r, g, b, a). */
fun decodePng(data: ByteArray): List<IntArray> {
    require(data.copyOfRange(0, 8).contentEquals(byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10))) { "not a PNG" }
    var (width, height, depth, colourType, interlace) = listOf(0, 0, 0, 0, 0)
    var palette: List<IntArray>? = null
    var transparency: ByteArray? = null
    val idat = ByteArrayOutputStream()
    var pos = 8
    while (pos < data.size) {
        val length = ByteBuffer.wrap(data, pos, 4).int
        val kind = String(data, pos + 4, 4, Charsets.US_ASCII)
        val body = data.copyOfRange(pos + 8, pos + 8 + length)
        pos += 12 + length
        when (kind) {
            "IHDR" -> ByteBuffer.wrap(body).let {
                width = it.int
                height = it.int
                depth = it.get().toInt() and 0xff
                colourType = it.get().toInt() and 0xff
                it.get(); it.get()
                interlace = it.get().toInt()
            }
            "PLTE" -> palette = (body.indices step 3).map { i -> IntArray(3) { body[i + it].toInt() and 0xff } }
            "tRNS" -> transparency = body
            "IDAT" -> idat.write(body)
        }
        if (kind == "IEND") break
    }
    require(interlace == 0) { "interlaced PNGs are not supported" }

    val channels = CHANNELS.getValue(colourType)
    val bits = channels * depth
    val bpp = maxOf(1, bits / 8)
    val stride = (width * bits + 7) / 8
    val raw = Inflater().run {
        setInput(idat.toByteArray())
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(65536)
        while (!finished()) out.write(buffer, 0, inflate(buffer))
        out.toByteArray()
    }

    val rows = mutableListOf<IntArray>()
    var prev = IntArray(stride)
    for (y in 0 until height) {
        val start = y * (stride + 1)
        val filter = raw[start].toInt()
        val line = IntArray(stride) { raw[start + 1 + it].toInt() and 0xff }
        for (i in 0 until stride) {
            val a = if (i >= bpp) line[i - bpp] else 0
            val b = prev[i]
            val c = if (i >= bpp) prev[i - bpp] else 0
            line[i] = when (filter) {
                1 -> line[i] + a
                2 -> line[i] + b
                3 -> line[i] + (a + b) / 2
                4 -> {
                    val p = a + b - c
                    val (pa, pb, pc) = Triple(Math.abs(p - a), Math.abs(p - b), Math.abs(p - c))
                    line[i] + if (pa <= pb && pa <= pc) a else if (pb <= pc) b else c
                }
                else -> line[i]
            } and 0xff
        }
        rows += line
        prev = line
    }

    val pixels = mutableListOf<IntArray>()
    for (line in rows) {
        val samples = if (depth < 8) {
            val mask = (1 shl depth) - 1
            IntArray(width) { i -> (line[i * depth / 8] shr (8 - depth - (i * depth) % 8)) and mask }
        } else {
            val step = depth / 8 // 16-bit samples keep the high byte
            IntArray((line.size + step - 1) / step) { line[it * step] }
        }
        for (x in 0 until width) {
            val px = samples.copyOfRange(x * channels, (x + 1) * channels)
            pixels += when (colourType) {
                3 -> {
                    val (r, g, b) = palette!![px[0]]
                    val t = transparency
                    intArrayOf(r, g, b, if (t != null && t.isNotEmpty() && px[0] < t.size) t[px[0]].toInt() and 0xff else 255)
                }
                0, 4 -> {
                    val v = if (depth < 8) px[0] * 255 / ((1 shl depth) - 1) else px[0]
                    intArrayOf(v, v, v, if (colourType == 4) px[1] else 255)
                }
                else -> intArrayOf(px[0], px[1], px[2], if (colourType == 6) px[3] else 255)
            }
        }
    }
    return pixels
}

// --- palette -----------------------------------------------------------------------------------

data class Rgb(val r: Int, val g: Int, val b: Int)

fun luma(c: Rgb): Double = 0.2126 * c.r + 0.7152 * c.g + 0.0722 * c.b

/** Python's colorsys.rgb_to_hsv hue. */
fun hue(c: Rgb): Double {
    val (r, g, b) = Triple(c.r / 255.0, c.g / 255.0, c.b / 255.0)
    val (maxc, minc) = maxOf(r, g, b) to minOf(r, g, b)
    if (maxc == minc) return 0.0
    val range = maxc - minc
    val (rc, gc, bc) = Triple((maxc - r) / range, (maxc - g) / range, (maxc - b) / range)
    val h = when (maxc) {
        r -> bc - gc
        g -> 2.0 + rc - bc
        else -> 4.0 + gc - rc
    } / 6.0
    val m = h % 1.0
    return if (m != 0.0 && m < 0) m + 1.0 else m
}

val SORTS: Map<String, Comparator<Map.Entry<Rgb, Int>>> = mapOf(
    "luma" to compareBy { luma(it.key) },
    "hue" to compareBy({ hue(it.key) }, { luma(it.key) }),
    "count" to compareBy { -it.value },
)

fun collect(source: Source, names: List<String>, alphaMin: Int): Map<Rgb, Int> {
    val counts = LinkedHashMap<Rgb, Int>()
    for (name in names) {
        for ((r, g, b, a) in decodePng(source.read(name))) {
            if (a >= alphaMin) counts.merge(Rgb(r, g, b), 1, Int::plus)
        }
    }
    return counts
}

fun writeGpl(path: File, title: String, counts: Map<Rgb, Int>, sort: String, columns: Int) {
    val items = counts.entries.sortedWith(SORTS.getValue(sort))
    val lines = listOf("GIMP Palette", "Name: $title", "Columns: $columns", "#") + items.map { (c, n) ->
        "%3d %3d %3d\t#%02x%02x%02x (%dpx)".format(c.r, c.g, c.b, c.r, c.g, c.b, n)
    }
    path.parentFile.mkdirs()
    path.writeText(lines.joinToString("\n") + "\n")
    val cwd = File("").absoluteFile
    val shown = if (path.absoluteFile.startsWith(cwd)) path.absoluteFile.relativeTo(cwd) else path
    println("$shown: ${items.size} colours")
}

/** fnmatch.fnmatchcase: * and ? match anything, including '/'. */
fun matches(name: String, pattern: String): Boolean = Regex(
    pattern.map { c ->
        when (c) {
            '*' -> ".*"
            '?' -> "."
            else -> Regex.escape(c.toString())
        }
    }.joinToString(""),
).matches(name)

// --- arguments ---------------------------------------------------------------------------------

val USAGE = """
    usage: vanilla_palette.main.kts TEXTURE... [--jar JAR | --dir DIR] [--version V] [--merge NAME]
                                    [--out DIR] [--sort luma|hue|count] [--columns N] [--alpha-min N]

      TEXTURE          texture paths like block/granite; wildcards allowed
      --jar JAR        Minecraft client jar to read from
      --dir DIR        folder of extracted textures (assets/minecraft/textures)
      --version V      Minecraft version to download (default: gradle.properties)
      --merge NAME     write one combined palette NAME.gpl instead of one per texture
      --out DIR        output folder (default: art/palettes)
      --sort ORDER     colour order: luma, hue or count (default: luma)
      --columns N      swatch columns (default: 8)
      --alpha-min N    skip pixels with alpha below this (default: 1, i.e. only fully clear)
""".trimIndent()

val options = mutableMapOf<String, String>()
val textures = mutableListOf<String>()
run {
    val queue = ArrayDeque(args.toList())
    while (queue.isNotEmpty()) {
        val arg = queue.removeFirst()
        when {
            arg == "-h" || arg == "--help" -> { println(USAGE); exitProcess(0) }
            arg.startsWith("--") -> options[arg.removePrefix("--")] = queue.removeFirstOrNull() ?: fail("$arg needs a value\n$USAGE")
            else -> textures += arg
        }
    }
}
val unknown = options.keys - setOf("jar", "dir", "version", "merge", "out", "sort", "columns", "alpha-min")
if (unknown.isNotEmpty()) fail("unknown option --${unknown.first()}\n$USAGE")
if (textures.isEmpty()) fail("at least one TEXTURE is required\n$USAGE")
if ("jar" in options && "dir" in options) fail("--jar and --dir can't be used together")
val sort = options["sort"] ?: "luma"
if (sort !in SORTS) fail("--sort must be one of ${SORTS.keys.joinToString()}")
val columns = options["columns"]?.toInt() ?: 8
val alphaMin = options["alpha-min"]?.toInt() ?: 1

val source: Source = options["dir"]?.let { DirSource(File(it)) }
    ?: JarSource(options["jar"]?.let(::File) ?: clientJar(options["version"] ?: minecraftVersion()))

val names = mutableListOf<String>()
for (texture in textures) {
    val pattern = texture.removeSuffix(".png")
    val matched = source.names.filter { matches(it, pattern) }.sorted()
    if (matched.isEmpty()) fail("no texture matches '$pattern'")
    names += matched.filter { it !in names }
}

val out = options["out"]?.let(::File) ?: ART_DIR.resolve("palettes")
val merge = options["merge"]
if (merge != null) {
    writeGpl(out.resolve("$merge.gpl"), merge, collect(source, names, alphaMin), sort, columns)
} else {
    for (name in names) {
        val title = name.substringAfterLast('/')
        writeGpl(out.resolve("$title.gpl"), title, collect(source, listOf(name), alphaMin), sort, columns)
    }
}
