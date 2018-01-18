package com.pavel.augmented.presentation.ar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import cn.easyar.Matrix44F
import cn.easyar.Vec2F
import com.pavel.augmented.events.BitmapLoaded
import org.greenrobot.eventbus.EventBus
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SketchRenderer {

    private val vertices = floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f)

    private val textureVertices = floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f)

    private val vertexShaderCode = "attribute vec4 aPosition;" +
            "uniform mat4 trans;" +
            "uniform mat4 proj;" +
            "uniform mat4 rotation;" +
            "attribute vec2 aTexPosition;" +
            "varying vec2 vTexPosition;" +
            "varying vec4 vcolor;" +

            "void main() {" +
            "  gl_Position = proj*trans*rotation*aPosition;" +
            "  vTexPosition = aTexPosition;" +
            "}"

    private val fragmentShaderCode = "precision mediump float;" +
            "uniform sampler2D uTexture;" +
            "varying vec2 vTexPosition;" +
            "varying vec4 vcolor;" +

            "void main() {" +
            "  gl_FragColor = texture2D(uTexture, vTexPosition);" +
            "}"

    private var verticesBuffer: FloatBuffer? = null
    private var textureBuffer: FloatBuffer? = null

    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0
    private var program: Int = 0
    private var texture: Int = 0

    fun init() {
        initializeBuffers()
        initializeProgram()
    }


    private fun initializeBuffers() {
        var buff = ByteBuffer.allocateDirect(vertices.size * 4)
        buff.order(ByteOrder.nativeOrder())
        verticesBuffer = buff.asFloatBuffer()
        verticesBuffer!!.put(vertices)
        verticesBuffer!!.position(0)

        buff = ByteBuffer.allocateDirect(textureVertices.size * 4)
        buff.order(ByteOrder.nativeOrder())
        textureBuffer = buff.asFloatBuffer()
        textureBuffer!!.put(textureVertices)
        textureBuffer!!.position(0)
    }

    private fun initializeProgram() {
        vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vertexShaderCode)
        GLES20.glCompileShader(vertexShader)

        fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES20.glCompileShader(fragmentShader)

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)

        GLES20.glLinkProgram(program)
    }

    fun render(projectionMatrix: Matrix44F, cameraview: Matrix44F, size: Vec2F) {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        val texturePositionHandle = GLES20.glGetAttribLocation(program, "aTexPosition")
        val trans = GLES20.glGetUniformLocation(program, "trans")
        val proj = GLES20.glGetUniformLocation(program, "proj")
        val rot = GLES20.glGetUniformLocation(program, "rotation")

        GLES20.glVertexAttribPointer(texturePositionHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        GLES20.glEnableVertexAttribArray(texturePositionHandle)

        val rotationMatrix = floatArrayOf(0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F, 0F)
        Matrix.setRotateM(rotationMatrix, 0, 180F, 0F, 1F, 0F)

        GLES20.glUniformMatrix4fv(trans, 1, false, cameraview.data, 0)
        GLES20.glUniformMatrix4fv(proj, 1, false, projectionMatrix.data, 0)
        GLES20.glUniformMatrix4fv(rot, 1, false, rotationMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, verticesBuffer)
        GLES20.glEnableVertexAttribArray(positionHandle)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }

    fun loadTexture(bitmap: Bitmap?) {
        bitmap?.let {
            val textureHandle = IntArray(1)
            GLES20.glGenTextures(1, textureHandle, 0)
            if (textureHandle[0] != 0) {
                val options = BitmapFactory.Options()
                options.inScaled = false   // No pre-scaling
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
                EventBus.getDefault().post(BitmapLoaded())
//                bitmap.recycle()
            }

            if (textureHandle[0] == 0) {
                throw RuntimeException("Error loading texture.")
            }

            texture = textureHandle[0]
        }

    }

}
