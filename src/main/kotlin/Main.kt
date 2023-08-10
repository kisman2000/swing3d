import java.awt.Color
import java.awt.Graphics
import java.awt.event.*
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.*

const val RAD = 0.017453292

const val WIDTH = 1000
const val HEIGHT = 1000

const val KEY_W = 87
const val KEY_A = 65
const val KEY_S = 83
const val KEY_D = 68
const val KEY_SPACE = 32
const val KEY_LSHIFT = 16

const val fov = 2
const val a = (fov / 180f) * PI

val center = vec2(0.5, 0.5)
val aspect = vec3(4, 4, 8)
val camera = Camera(vec3(0, 0, 0), rotation(0, 0))

val movementSpeed = vec3(1, 1, 1)
val movementInput = MovementInput(1.0, 1.0)

const val sensitivity = 0.5

var prevMouse = vec2(-1, -1)
var deltaMouse = vec2(-1, -1)

val lines = mutableListOf<Line>()
val polygons = mutableListOf<Polygon>()

fun render() {
    cube(vec3(0, 0, 2), 1, Color.RED, Color.BLACK)
    cube(vec3(3, 0, 3), 2, null, Color.BLUE)

    Box(
        vec3(5, 0, 0),
        vec3(-5, 0, 0),
        null,
        Color.BLACK
    )

    Box(
        vec3(0, 5, 0),
        vec3(0, -5, 0),
        null,
        Color.BLACK
    )

    Box(
        vec3(0, 0, 5),
        vec3(0, 0, -5),
        null,
        Color.BLACK
    )

    cube(vec3(8 + 3, 8, 8 + 3), 1, Color.GREEN, Color.BLACK)
    cube(vec3(8, 8, 8), 1, Color.GREEN, Color.BLACK)
    cube(vec3(8 - 3, 8, 8), 1, Color.GREEN, Color.BLACK)
    cube(vec3(8, 8, 8 - 3), 1, Color.GREEN, Color.BLACK)
}

fun key(
    event : KeyEvent
) {
    when(event.keyCode) {
        KEY_W -> {
            val forward = (complex(0, 1) * complex(cos(RAD * camera.rotation.yaw), sin(RAD * camera.rotation.yaw))).vector() * movementInput.forward

            camera.position.xz += forward
        }
        KEY_A -> {
            val strafing = (complex(-1, 0) * complex(cos(RAD * camera.rotation.yaw), sin(RAD * camera.rotation.yaw))).vector() * movementInput.strafing

            camera.position.xz += strafing
        }
        KEY_S -> {
            val forward = (complex(0, -1) * complex(cos(RAD * camera.rotation.yaw), sin(RAD * camera.rotation.yaw))).vector() * movementInput.forward

            camera.position.xz += forward
        }
        KEY_D -> {
            val strafing = (complex(1, 0) * complex(cos(RAD * camera.rotation.yaw), sin(RAD * camera.rotation.yaw))).vector() * movementInput.strafing

            camera.position.xz += strafing
        }
        KEY_SPACE -> camera.position.y -= movementSpeed.y
        KEY_LSHIFT -> camera.position.y += movementSpeed.y
    }
}

fun mouse(
    event : MouseEvent
) {
    deltaMouse = vec2(event.x - prevMouse.x, event.y - prevMouse.y)
    prevMouse = vec2(event.x, event.y)

    camera.rotation.yaw += deltaMouse.x * sensitivity
    camera.rotation.pitch -= deltaMouse.y * sensitivity
}

fun main(
    args : Array<String>
) {
    val frame = JFrame().also {
        it.setSize(WIDTH, HEIGHT)
        it.setLocationRelativeTo(null)
        it.title = "Swing3d"
        it.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        it.isVisible = true
        it.addMouseListener(object : MouseAdapter() {

        })
        it.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(
                e : MouseEvent
            ) {
                mouse(e)
            }
        })
        it.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(
                e : KeyEvent
            ) {
                key(e)
            }
        })
    }

    val panel = object : JPanel() {
        override fun paintComponent(
            graphics : Graphics
        ) {
            super.paintComponent(graphics)

            graphics.color = Color.BLACK
            graphics.drawString(camera.position.toString(), 5, 10)

            for(polygon in polygons) {
                val x = IntArray(polygon.points.size)
                val y = IntArray(polygon.points.size)

                for((index, point) in polygon.points.withIndex()) {
                    point.refresh()

                    x[index] = point.position2d.x.toInt()
                    y[index] = point.position2d.y.toInt()
                }

                graphics.color = polygon.color

                graphics.fillPolygon(
                    x,
                    y,
                    polygon.points.size
                )
            }

            for(line in lines) {
                line.start.refresh()
                line.end.refresh()

                graphics.color = line.color

                graphics.drawLine(
                    line.start.position2d.x.toInt(),
                    line.start.position2d.y.toInt(),
                    line.end.position2d.x.toInt(),
                    line.end.position2d.y.toInt()
                )
            }
        }
    }

    render()

    frame.add(panel)

    while(true) {
        panel.updateUI()
        panel.validate()
        panel.repaint()
    }
}

fun modify(
    position : Vec2
) = vec2(position.x + center.x * WIDTH, position.y + center.y * HEIGHT)

fun project(
    position : Vec3
) : Vec2 {
    val x = (position.x + camera.position.x) / max(((position.z / aspect.z + camera.position.z) * tan(a / aspect.x)), 0.000000000001)
    val y = (position.y + camera.position.y) / max(((position.z / aspect.z + camera.position.z) * tan(a / aspect.y)), 0.000000000001)

    return vec2(x, y)
}

fun rotate(
    position : Vec2,
    degree : Double,
    center : Vec2
) : Vec2 {
    val vector = position - center
    val length = vector.length()
    val complex = vector.complex() * complex(cos(RAD * degree), sin(RAD * degree))
    val rotated = complex.vector().norm() * length

    return rotated + center
}

fun rotate(
    position : Vec3
) : Vec3 {
    val rotateY = rotate(position.xz, camera.rotation.yaw, camera.position.xz)
    val rotateX = rotate(vec2(position.y, rotateY.y), camera.rotation.pitch, camera.position.yz)

    return vec3(
        rotateY.x,
        rotateX.x,
        rotateY.y
    )
}

fun vec3(
    x : Number,
    y : Number,
    z : Number
) = Vec3(
    x.toDouble(),
    y.toDouble(),
    z.toDouble()
)

fun vec2(
    x : Number,
    y : Number,
) = Vec2(
    x.toDouble(),
    y.toDouble()
)

fun rotation(
    yaw : Number,
    pitch : Number
) = Rotation(
    yaw.toDouble(),
    pitch.toDouble()
)

fun line(
    start : Point,
    end : Point,
    color : Color?
) = Line(
    start,
    end,
    color ?: Color.BLACK
).also {
    if(color != null) {
        lines.add(it)
    }
}

fun polygon(
    color : Color?,
    vararg points : Point
) = Polygon(
    points.toList(),
    color ?: Color.WHITE
).also {
    if(color != null) {
        polygons.add(it)
    }
}

fun cube(
    center : Vec3,
    size : Number,
    fill : Color?,
    outline : Color?
) = Box(
    vec3(
        center.x - size.toDouble(),
        center.y - size.toDouble(),
        center.z - size.toDouble()
    ),
    vec3(
        center.x + size.toDouble(),
        center.y + size.toDouble(),
        center.z + size.toDouble()
    ),
    fill,
    outline
)

fun complex(
    real : Number,
    imag : Number
) = Complex(
    real.toDouble(),
    imag.toDouble()
)

class Vec3(
    var x : Double,
    var y : Double,
    var z : Double
) {
    val xx get() = Vec2(x, x)
    var xy get() = Vec2(x, y) ; set(vector) { x = vector.x ; y = vector.y }
    var xz get() = Vec2(x, z) ; set(vector) { x = vector.x ; z = vector.y }
    var yx get() = Vec2(y, x) ; set(vector) { y = vector.x ; x = vector.y }
    val yy get() = Vec2(y, y)
    var yz get() = Vec2(y, z) ; set(vector) { y = vector.x ; z = vector.y }
    var zx get() = Vec2(z, x) ; set(vector) { z = vector.x ; x = vector.y }
    var zy get() = Vec2(z, y) ; set(vector) { z = vector.x ; y = vector.y }
    val zz get() = Vec2(z, z)

    fun length() = sqrt(x * x + y * y + z * z)

    fun norm() : Vec3 {
        val length = length()

        return Vec3(x / length, y / length, z / length)
    }

    fun mult(
        multiplier : Number
    ) = Vec3(x * multiplier.toDouble(), y * multiplier.toDouble(), z * multiplier.toDouble())

    operator fun plus(
        offset : Vec3
    ) = vec3(
        x + offset.x,
        y + offset.y,
        z + offset.z
    )

    override fun toString() = "Vec3[$x;$y;$z]"
}

class Vec2(
    var x : Double,
    var y : Double
) {
    fun complex() = Complex(x, y)

    fun length() = sqrt(x * x + y * y)

    fun norm() : Vec2 {
        val length = length()

        return Vec2(x / length, y / length)
    }

    operator fun plus(
        vector : Vec2
    ) = Vec2(
        x + vector.x,
        y + vector.y
    )

    operator fun minus(
        vector : Vec2
    ) = Vec2(
        x - vector.x,
        y - vector.y
    )

    operator fun times(
        multiplier : Number
    ) = Vec2(
        x * multiplier.toDouble(),
        y * multiplier.toDouble()
    )

    override fun toString() = "Vec2[$x;$y]"
}

class Rotation(
    var yaw : Double,
    var pitch : Double
)

class Camera(
    val position : Vec3,
    val rotation : Rotation
)

class Box(
    private val start : Vec3,
    private val end : Vec3,
    private val fill : Color?,
    private val outline : Color?
) {
    init {
        val minX = min(start.x, end.x)
        val minY = min(start.y, end.y)
        val minZ = min(start.z, end.z)
        val maxX = max(start.x, end.x)
        val maxY = max(start.y, end.y)
        val maxZ = max(start.z, end.z)

        val point1 = Point(vec3(minX, minY, minZ))
        val point2 = Point(vec3(maxX, minY, minZ))
        val point3 = Point(vec3(maxX, minY, maxZ))
        val point4 = Point(vec3(minX, minY, maxZ))
        val point5 = Point(vec3(minX, maxY, minZ))
        val point6 = Point(vec3(maxX, maxY, minZ))
        val point7 = Point(vec3(maxX, maxY, maxZ))
        val point8 = Point(vec3(minX, maxY, maxZ))

        val line01 = line(point1, point2, outline)
        val line02 = line(point2, point3, outline)
        val line03 = line(point3, point4, outline)
        val line04 = line(point4, point1, outline)
        val line05 = line(point5, point6, outline)
        val line06 = line(point6, point7, outline)
        val line07 = line(point7, point8, outline)
        val line08 = line(point8, point5, outline)
        val line09 = line(point1, point5, outline)
        val line10 = line(point2, point6, outline)
        val line11 = line(point3, point7, outline)
        val line12 = line(point4, point8, outline)

        val polygon1 = polygon(fill, point1, point2, point3, point4)
        val polygon2 = polygon(fill, point5, point6, point7, point8)
        val polygon3 = polygon(fill, point1, point2, point6, point5)
        val polygon4 = polygon(fill, point3, point4, point8, point7)
        val polygon5 = polygon(fill, point1, point4, point8, point5)
        val polygon6 = polygon(fill, point2, point3, point7, point6)
    }
}

class Point(
    var position3d : Vec3,
    var position2d : Vec2 = modify(project(rotate(position3d)))
) {
    fun refresh() {
        position2d = modify(project(rotate(position3d)))
    }

    operator fun plus(
        offset : Vec3
    ) = Point(
        position3d + offset
    )

    override fun toString() = "Point[$position3d;$position2d]"
}

class Line(
    val start : Point,
    val end : Point,
    val color : Color
) {
    override fun toString() = "Line[$start;$end]"
}

class Polygon(
    val points : List<Point>,
    val color : Color
) {
    override fun toString() = "Polygon[${points.joinToString(";") { it.toString() }}]"
}

class Complex(
    var real : Double,
    var imag : Double
) {
    fun vector() = Vec2(real, imag)

    operator fun times(
        complex : Complex
    ) = Complex(
        real * complex.real - imag * complex.imag,
        imag * complex.real + real * complex.imag
    )
}

class MovementInput(
    var forward : Double,
    var strafing : Double
)