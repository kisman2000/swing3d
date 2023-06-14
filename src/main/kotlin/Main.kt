import java.awt.Color
import java.awt.Graphics
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.*

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

val movementSpeed = vec3(5, 5, 5)

const val sensitivity = 0.5

var prevMouse = vec2(-1, -1)
var deltaMouse = vec2(-1, -1)

val lines = mutableListOf<Line>()

fun render() {
    cube(vec3(0, 0, 2), 1, null, Color.BLACK)
}

fun key(
    event : KeyEvent
) {
    when(event.keyCode) {
        KEY_W -> camera.position.z += movementSpeed.z
        KEY_A -> camera.position.x -= movementSpeed.x
        KEY_S -> camera.position.z -= movementSpeed.z
        KEY_D -> camera.position.x += movementSpeed.x
        KEY_SPACE -> camera.position.y -= movementSpeed.y
        KEY_LSHIFT -> camera.position.y += movementSpeed.y
    }
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
    yaw.toFloat(),
    pitch.toFloat()
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
    lines.add(it)
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

class Vec3(
    var x : Double,
    var y : Double,
    var z : Double
) {
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
    fun length() = sqrt(x * x + y * y)

    fun norm() : Vec2 {
        val length = length()

        return Vec2(x / length, y / length)
    }

    fun mult(
        multiplier : Number
    ) = Vec2(x * multiplier.toDouble(), y * multiplier.toDouble())

    override fun toString() = "Vec2[$x;$y]"
}

class Rotation(
    var yaw : Float,
    var pitch : Float
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
    }
}

class Point(
    var position3d : Vec3,
    var position2d : Vec2 = modify(project(position3d))
) {
    fun refresh() {
        position2d = modify(project(position3d))
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
)