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

//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

class SketchRenderer {
    private var program_box: Int = 0
    private var pos_coord_box: Int = 0
    private var pos_color_box: Int = 0
    private var pos_trans_box: Int = 0
    private var pos_proj_box: Int = 0
    private var vbo_coord_box: Int = 0
    private var vbo_color_box: Int = 0
    private var vbo_color_box_2: Int = 0
    private var vbo_faces_box: Int = 0

    private var textureUniformHandle: Int = 0
    private var textureCoordinateHandle: Int = 0
    private val textureCoordinateDataSize = 2
    private val textureDataHandle: Int? = null

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

    private fun generateOneBuffer(): Int {
        val buffer = intArrayOf(0)
        GLES20.glGenBuffers(1, buffer, 0)
        return buffer[0]
    }

    fun init() {
        initializeBuffers()
        initializeProgram()
    }

//    fun init() {
//        val box_vert = """uniform mat4 trans;
//uniform mat4 proj;
//attribute vec4 coord;
//attribute vec4 color;
//attribute vec2 a_TexCoordinate;
//varying vec2 v_TexCoordinate;
//varying vec4 vcolor;
//
//void main(void)
//{
//    vcolor = color;
//    gl_Position = proj*trans*coord;
//    v_TexCoordinate = a_TexCoordinate;
//}
//"""
//
//        val box_frag = """#ifdef GL_ES
//precision highp float;
//#endif
//varying vec4 vcolor;
//uniform sampler2D uTexture;
//varying vec2 v_TexCoordinate;
//
//void main(void)
//{
//    gl_FragColor = texture2D(uTexture, v_TexCoordinate);
//}
//"""
//
////        val fragmentShaderCode = "precision mediump float;" +
////        "uniform vec4 vсolor;" +
////         //Test
////                            "uniform sampler2D u_Texture;" +
////        "varying vec2 v_TexCoordinate;" +
////         //End Test
////                            "void main() {" +
////         //"gl_FragColor = vColor;" +
////                            "gl_FragColor = (vсolor * texture2D(u_Texture, v_TexCoordinate));" +
////        "}"
//
//        program_box = GLES20.glCreateProgram()
//        val vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
//        GLES20.glShaderSource(vertShader, box_vert)
//        GLES20.glCompileShader(vertShader)
//        val fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
//        GLES20.glShaderSource(fragShader, box_frag)
//        GLES20.glCompileShader(fragShader)
//        GLES20.glAttachShader(program_box, vertShader)
//        GLES20.glAttachShader(program_box, fragShader)
//        GLES20.glLinkProgram(program_box)
//        GLES20.glUseProgram(program_box)
//        pos_coord_box = GLES20.glGetAttribLocation(program_box, "coord")
//        pos_color_box = GLES20.glGetAttribLocation(program_box, "color")
//        pos_trans_box = GLES20.glGetUniformLocation(program_box, "trans")
//        pos_proj_box = GLES20.glGetUniformLocation(program_box, "proj")
//
//        vbo_coord_box = generateOneBuffer()
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
//        val cube_vertices = arrayOf(
//                /* +z */floatArrayOf(1.0f / 2, 1.0f / 2, 0.01f / 2), floatArrayOf(1.0f / 2, -1.0f / 2, 0.01f / 2), floatArrayOf(-1.0f / 2, -1.0f / 2, 0.01f / 2), floatArrayOf(-1.0f / 2, 1.0f / 2, 0.01f / 2),
//                /* -z */floatArrayOf(1.0f / 2, 1.0f / 2, -0.01f / 2), floatArrayOf(1.0f / 2, -1.0f / 2, -0.01f / 2), floatArrayOf(-1.0f / 2, -1.0f / 2, -0.01f / 2), floatArrayOf(-1.0f / 2, 1.0f / 2, -0.01f / 2))
//        val cube_vertices_buffer = FloatBuffer.wrap(cube_vertices.asIterable().flatMap { it.asIterable() }.toFloatArray())
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW)
//
//        vbo_faces_box = generateOneBuffer()
//        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box)
//        val cube_faces = arrayOf(
//                /* +z */floatArrayOf(1.0f / 2, 1.0f / 2, 0.01f / 2), floatArrayOf(1.0f / 2, -1.0f / 2, 0.01f / 2), floatArrayOf(-1.0f / 2, -1.0f / 2, 0.01f / 2), floatArrayOf(-1.0f / 2, 1.0f / 2, 0.01f / 2),
//                /* -z */floatArrayOf(1.0f / 2, 1.0f / 2, -0.01f / 2), floatArrayOf(1.0f / 2, -1.0f / 2, -0.01f / 2), floatArrayOf(-1.0f / 2, -1.0f / 2, -0.01f / 2), floatArrayOf(-1.0f / 2, 1.0f / 2, -0.01f / 2))
//        val cube_faces_buffer = FloatBuffer.wrap(cube_faces.asIterable().flatMap { it.asIterable() }.toFloatArray())
//        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, cube_faces_buffer.limit() * 2, cube_faces_buffer, GLES20.GL_STATIC_DRAW)
//    }

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
        val (size0, size1) = size.data

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
        val height = size0 / 1000
        val cube_vertices = arrayOf(
                /* +z */floatArrayOf(size0 / 2, size1 / 2, height / 2), floatArrayOf(size0 / 2, -size1 / 2, height / 2), floatArrayOf(-size0 / 2, -size1 / 2, height / 2), floatArrayOf(-size0 / 2, size1 / 2, height / 2),
                /* -z */floatArrayOf(size0 / 2, size1 / 2, 0f), floatArrayOf(size0 / 2, -size1 / 2, 0f), floatArrayOf(-size0 / 2, -size1 / 2, 0f), floatArrayOf(-size0 / 2, size1 / 2, 0f))
        val cube_vertices_buffer = FloatBuffer.wrap(cube_vertices.asIterable().flatMap { it.asIterable() }.toFloatArray())
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glUseProgram(program_box)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
        GLES20.glEnableVertexAttribArray(pos_coord_box)
        GLES20.glVertexAttribPointer(pos_coord_box, 3, GLES20.GL_FLOAT, false, 0, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box)
        GLES20.glEnableVertexAttribArray(pos_color_box)
        GLES20.glVertexAttribPointer(pos_color_box, 4, GLES20.GL_UNSIGNED_BYTE, true, 0, 0)
        GLES20.glUniformMatrix4fv(pos_trans_box, 1, false, cameraview.data, 0)
        GLES20.glUniformMatrix4fv(pos_proj_box, 1, false, projectionMatrix.data, 0)

        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
//        val cube_vertices_2 = arrayOf(
//                /* +z */floatArrayOf(size0 / 4, size1 / 4, size0 / 4), floatArrayOf(size0 / 4, -size1 / 4, size0 / 4), floatArrayOf(-size0 / 4, -size1 / 4, size0 / 4), floatArrayOf(-size0 / 4, size1 / 4, size0 / 4),
//                /* -z */floatArrayOf(size0 / 4, size1 / 4, 0f), floatArrayOf(size0 / 4, -size1 / 4, 0f), floatArrayOf(-size0 / 4, -size1 / 4, 0f), floatArrayOf(-size0 / 4, size1 / 4, 0f))
//        val cube_vertices_2_buffer = FloatBuffer.wrap(cube_vertices_2.asIterable().flatMap { it.asIterable() }.toFloatArray())
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_2_buffer.limit() * 4, cube_vertices_2_buffer, GLES20.GL_DYNAMIC_DRAW)
//        GLES20.glEnableVertexAttribArray(pos_coord_box)
//        GLES20.glVertexAttribPointer(pos_coord_box, 3, GLES20.GL_FLOAT, false, 0, 0)
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box_2)
//        GLES20.glEnableVertexAttribArray(pos_color_box)
//        GLES20.glVertexAttribPointer(pos_color_box, 4, GLES20.GL_UNSIGNED_BYTE, true, 0, 0)
//        for (i in 0..5) {
//            GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, i * 4 * 2)
//        }
    }

    fun render2(projectionMatrix: Matrix44F, cameraview: Matrix44F, size: Vec2F) {
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
