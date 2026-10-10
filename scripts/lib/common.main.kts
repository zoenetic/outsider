// Helpers shared by the scripts (pull in with @file:Import("lib/common.main.kts")).
//
// Several of them reproduce Python behaviour exactly, so the outputs match what the Python
// versions of these scripts wrote: json.dumps formatting, random.Random seeded with a string,
// and round() (half to even).

@file:DependsOn("com.google.code.gson:gson:2.13.2")

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import java.awt.image.BufferedImage
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.security.MessageDigest
import javax.imageio.ImageIO

// --- numbers -------------------------------------------------------------------------------------

fun clamp(v: Double, lo: Double = 0.0, hi: Double = 1.0): Double = maxOf(lo, minOf(hi, v))

/** Python's round(x): to the nearest integer, halves to even. */
fun pyRound(x: Double): Int = Math.rint(x).toInt()

/** Python's round(x, digits) for a float. */
fun pyRound(x: Double, digits: Int): Double =
    BigDecimal(x).setScale(digits, RoundingMode.HALF_EVEN).toDouble()

// --- JSON ----------------------------------------------------------------------------------------
// Parsed JSON is plain Kotlin: MutableMap<String, Any?> (insertion-ordered), MutableList<Any?>,
// String, Boolean, null, and Double for every number.

fun parseJson(text: String): Any? = fromGson(JsonParser.parseString(text))

private fun fromGson(element: JsonElement): Any? = when {
    element.isJsonNull -> null
    element.isJsonObject -> element.asJsonObject.entrySet()
        .associateTo(LinkedHashMap<String, Any?>()) { (k, v) -> k to fromGson(v) }
    element.isJsonArray -> element.asJsonArray.mapTo(mutableListOf()) { fromGson(it) }
    element.asJsonPrimitive.isBoolean -> element.asBoolean
    element.asJsonPrimitive.isNumber -> element.asDouble
    else -> element.asString
}

@Suppress("UNCHECKED_CAST")
fun Any?.obj(): MutableMap<String, Any?> = this as MutableMap<String, Any?>

@Suppress("UNCHECKED_CAST")
fun Any?.list(): MutableList<Any?> = this as MutableList<Any?>

fun Any?.num(): Double = this as Double

@Suppress("UNCHECKED_CAST")
fun <T> deepCopy(value: T): T = when (value) {
    is Map<*, *> -> value.entries.associateTo(LinkedHashMap<String, Any?>()) { (k, v) -> k as String to deepCopy(v) }
    is List<*> -> value.mapTo(mutableListOf()) { deepCopy(it) }
    else -> value
} as T

/** json.dumps(value, indent=indent) + "\n", byte for byte. */
fun pyJson(value: Any?, indent: String = "  "): String = StringBuilder().also { write(it, value, indent, 0) }.append('\n').toString()

private fun write(out: StringBuilder, value: Any?, indent: String, depth: Int) {
    fun newline(d: Int) = out.append('\n').append(indent.repeat(d))
    when (value) {
        null -> out.append("null")
        is Boolean -> out.append(value)
        is Int, is Long -> out.append(value)
        is Double -> out.append(pyFloat(value))
        is String -> quote(out, value)
        is Map<*, *> -> if (value.isEmpty()) out.append("{}") else {
            out.append('{')
            value.entries.forEachIndexed { i, (k, v) ->
                if (i > 0) out.append(',')
                newline(depth + 1)
                quote(out, k as String)
                out.append(": ")
                write(out, v, indent, depth + 1)
            }
            newline(depth)
            out.append('}')
        }
        is List<*> -> if (value.isEmpty()) out.append("[]") else {
            out.append('[')
            value.forEachIndexed { i, v ->
                if (i > 0) out.append(',')
                newline(depth + 1)
                write(out, v, indent, depth + 1)
            }
            newline(depth)
            out.append(']')
        }
        else -> error("can't write ${value::class} as JSON")
    }
}

/** Python's repr(float): shortest round-trip digits, scientific below 1e-4 and from 1e16. */
fun pyFloat(x: Double): String {
    if (x.isNaN()) return "NaN"
    if (x.isInfinite()) return if (x > 0) "Infinity" else "-Infinity"
    if (x == 0.0) return if (1 / x < 0) "-0.0" else "0.0"
    val decimal = BigDecimal(x.toString()).stripTrailingZeros()
    val digits = decimal.unscaledValue().abs().toString()
    val exponent = digits.length - 1 - decimal.scale()
    val sign = if (x < 0) "-" else ""
    if (exponent in -4..15) {
        val plain = decimal.abs().toPlainString()
        return sign + if ('.' in plain) plain else "$plain.0"
    }
    val mantissa = if (digits.length == 1) digits else digits[0] + "." + digits.substring(1)
    return "$sign${mantissa}e${if (exponent < 0) '-' else '+'}${Math.abs(exponent).toString().padStart(2, '0')}"
}

private fun quote(out: StringBuilder, s: String) {
    out.append('"')
    for (c in s) {
        when (c) {
            '"' -> out.append("\\\"")
            '\\' -> out.append("\\\\")
            '\n' -> out.append("\\n")
            '\r' -> out.append("\\r")
            '\t' -> out.append("\\t")
            '\b' -> out.append("\\b")
            '\u000c' -> out.append("\\f")
            else -> if (c < ' ' || c > '~') out.append("\\u%04x".format(c.code)) else out.append(c)
        }
    }
    out.append('"')
}

// --- random --------------------------------------------------------------------------------------

/** Python's random.Random(seed).random() for a string seed: MT19937, seeded as CPython does. */
class PyRandom(seed: String) {
    private val mt = LongArray(624)
    private var index = 624

    init {
        val bytes = seed.toByteArray(Charsets.UTF_8)
        val n = BigInteger(1, bytes + MessageDigest.getInstance("SHA-512").digest(bytes))
        val words = maxOf(1, (n.bitLength() + 31) / 32)
        initByArray(LongArray(words) { n.shiftRight(32 * it).toLong() and 0xffffffffL })
    }

    private fun initGenrand(s: Long) {
        mt[0] = s
        for (i in 1 until 624) {
            mt[i] = (1812433253L * (mt[i - 1] xor (mt[i - 1] ushr 30)) + i) and 0xffffffffL
        }
    }

    private fun initByArray(key: LongArray) {
        initGenrand(19650218L)
        var i = 1
        var j = 0
        repeat(maxOf(624, key.size)) {
            mt[i] = ((mt[i] xor ((mt[i - 1] xor (mt[i - 1] ushr 30)) * 1664525L)) + key[j] + j) and 0xffffffffL
            i++; j++
            if (i >= 624) { mt[0] = mt[623]; i = 1 }
            if (j >= key.size) j = 0
        }
        repeat(623) {
            mt[i] = ((mt[i] xor ((mt[i - 1] xor (mt[i - 1] ushr 30)) * 1566083941L)) - i) and 0xffffffffL
            i++
            if (i >= 624) { mt[0] = mt[623]; i = 1 }
        }
        mt[0] = 0x80000000L
    }

    private fun next32(): Long {
        if (index >= 624) {
            for (k in 0 until 624) {
                val y = (mt[k] and 0x80000000L) or (mt[(k + 1) % 624] and 0x7fffffffL)
                mt[k] = mt[(k + 397) % 624] xor (y ushr 1) xor (if (y and 1L != 0L) 0x9908b0dfL else 0L)
            }
            index = 0
        }
        var y = mt[index++]
        y = y xor (y ushr 11)
        y = y xor ((y shl 7) and 0x9d2c5680L)
        y = y xor ((y shl 15) and 0xefc60000L)
        return y xor (y ushr 18)
    }

    fun random(): Double {
        val a = next32() ushr 5
        val b = next32() ushr 6
        return (a * 67108864.0 + b) * (1.0 / 9007199254740992.0)
    }
}

// --- images --------------------------------------------------------------------------------------
// Colours are IntArray(r, g, b, a), like Pillow's RGBA tuples.

fun readImage(file: File): BufferedImage = readImage(file.readBytes())

fun readImage(bytes: ByteArray): BufferedImage {
    val source = ImageIO.read(bytes.inputStream()) ?: error("not an image")
    val (w, h) = source.width to source.height
    return newImage(w, h).also { it.setRGB(0, 0, w, h, source.getRGB(0, 0, w, h, null, 0, w), 0, w) }
}

fun BufferedImage.rgba(x: Int, y: Int): IntArray {
    val argb = getRGB(x, y)
    return intArrayOf(argb shr 16 and 0xff, argb shr 8 and 0xff, argb and 0xff, argb ushr 24)
}

fun BufferedImage.set(x: Int, y: Int, c: IntArray) = setRGB(x, y, (c[3] shl 24) or (c[0] shl 16) or (c[1] shl 8) or c[2])

/** All pixels, row by row, like Pillow's get_flattened_data(). */
fun BufferedImage.pixels(): List<IntArray> = (0 until height).flatMap { y -> (0 until width).map { x -> rgba(x, y) } }

fun BufferedImage.copy(): BufferedImage =
    BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also { it.setRGB(0, 0, width, height, getRGB(0, 0, width, height, null, 0, width), 0, width) }

fun BufferedImage.save(file: File) {
    file.parentFile.mkdirs()
    check(ImageIO.write(this, "png", file)) { "no PNG writer" }
}

fun newImage(width: Int, height: Int) = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

/** Python's round(a + (b - a) * t) per channel. */
fun lerp(a: IntArray, b: IntArray, t: Double): IntArray = IntArray(a.size) { pyRound(a[it] + (b[it] - a[it]) * t) }
