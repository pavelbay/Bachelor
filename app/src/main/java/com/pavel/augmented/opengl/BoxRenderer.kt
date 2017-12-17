package com.pavel.augmented.opengl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import cn.easyar.Matrix44F
import cn.easyar.Vec2F
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer

//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

class BoxRenderer {
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

    private fun generateOneBuffer(): Int {
        val buffer = intArrayOf(0)
        GLES20.glGenBuffers(1, buffer, 0)
        return buffer[0]
    }

    fun init() {
        val box_vert = """uniform mat4 trans;
uniform mat4 proj;
attribute vec4 coord;
attribute vec4 color;
varying vec4 vcolor;

void main(void)
{
    vcolor = color;
    gl_Position = proj*trans*coord;
}
"""

        val box_frag = """#ifdef GL_ES
precision highp float;
#endif
varying vec4 vcolor;

void main(void)
{
    gl_FragColor = vcolor;
}
"""

        program_box = GLES20.glCreateProgram()
        val vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertShader, box_vert)
        GLES20.glCompileShader(vertShader)
        val fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragShader, box_frag)
        GLES20.glCompileShader(fragShader)
        GLES20.glAttachShader(program_box, vertShader)
        GLES20.glAttachShader(program_box, fragShader)
        GLES20.glLinkProgram(program_box)
        GLES20.glUseProgram(program_box)
        pos_coord_box = GLES20.glGetAttribLocation(program_box, "coord")
        pos_color_box = GLES20.glGetAttribLocation(program_box, "color")
        pos_trans_box = GLES20.glGetUniformLocation(program_box, "trans")
        pos_proj_box = GLES20.glGetUniformLocation(program_box, "proj")

        vbo_coord_box = generateOneBuffer()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
        val cube_vertices = arrayOf(
                /* +z */floatArrayOf(1.0f / 2, 1.0f / 2, 0.01f / 2), floatArrayOf(1.0f / 2, -1.0f / 2, 0.01f / 2), floatArrayOf(-1.0f / 2, -1.0f / 2, 0.01f / 2), floatArrayOf(-1.0f / 2, 1.0f / 2, 0.01f / 2),
                /* -z */floatArrayOf(1.0f / 2, 1.0f / 2, -0.01f / 2), floatArrayOf(1.0f / 2, -1.0f / 2, -0.01f / 2), floatArrayOf(-1.0f / 2, -1.0f / 2, -0.01f / 2), floatArrayOf(-1.0f / 2, 1.0f / 2, -0.01f / 2))
        val cube_vertices_buffer = FloatBuffer.wrap(cube_vertices.asIterable().flatMap { it.asIterable() }.toFloatArray())
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_buffer.limit() * 4, cube_vertices_buffer, GLES20.GL_DYNAMIC_DRAW)

        vbo_color_box = generateOneBuffer()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box)
        val cube_vertex_colors = arrayOf(intArrayOf(255, 0, 0, 128), intArrayOf(0, 255, 0, 128), intArrayOf(0, 0, 255, 128), intArrayOf(0, 0, 0, 128), intArrayOf(0, 255, 255, 128), intArrayOf(255, 0, 255, 128), intArrayOf(255, 255, 0, 128), intArrayOf(255, 255, 255, 128))
        val cube_vertex_colors_buffer = ByteBuffer.wrap(cube_vertex_colors.asIterable().flatMap { it.asIterable() }.map { it.toByte() }.toByteArray())
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertex_colors_buffer.limit(), cube_vertex_colors_buffer, GLES20.GL_STATIC_DRAW)

        vbo_color_box_2 = generateOneBuffer()
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box_2)
        val cube_vertex_colors_2 = arrayOf(intArrayOf(255, 0, 0, 255), intArrayOf(255, 255, 0, 255), intArrayOf(0, 255, 0, 255), intArrayOf(255, 0, 255, 255), intArrayOf(255, 0, 255, 255), intArrayOf(255, 255, 255, 255), intArrayOf(0, 255, 255, 255), intArrayOf(255, 0, 255, 255))
        val cube_vertex_colors_2_buffer = ByteBuffer.wrap(cube_vertex_colors_2.asIterable().flatMap { it.asIterable() }.map { it.toByte() }.toByteArray())
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertex_colors_2_buffer.limit(), cube_vertex_colors_2_buffer, GLES20.GL_STATIC_DRAW)

        vbo_faces_box = generateOneBuffer()
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box)
        val cube_faces = arrayOf(
                /* +z */shortArrayOf(3, 2, 1, 0), /* -y */shortArrayOf(2, 3, 7, 6), /* +y */shortArrayOf(0, 1, 5, 4),
                /* -x */shortArrayOf(3, 0, 4, 7), /* +x */shortArrayOf(1, 2, 6, 5), /* -z */shortArrayOf(4, 5, 6, 7))
        val cube_faces_buffer = ShortBuffer.wrap(cube_faces.asIterable().flatMap { it.asIterable() }.toShortArray())
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, cube_faces_buffer.limit() * 2, cube_faces_buffer, GLES20.GL_STATIC_DRAW)
    }

    fun loadTexture(bitmap: Bitmap?): Int? {
        bitmap?.let {
            val textureHandle = IntArray(1)
            GLES20.glGenTextures(1, textureHandle, 0)
            if (textureHandle[0] != 0) {
                val options = BitmapFactory.Options()
                options.inScaled = false   // No pre-scaling

                // Bind to the texture in OpenGL
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

                // Set filtering
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)

                // Load the bitmap into the bound texture.
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

                // Recycle the bitmap, since its data has been loaded into OpenGL.
                bitmap.recycle()
            }

            if (textureHandle[0] == 0) {
                throw RuntimeException("Error loading texture.")
            }

            return textureHandle[0]
        }

        return null
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
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, vbo_faces_box)
        for (i in 0..5) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, i * 4 * 2)
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_coord_box)
        val cube_vertices_2 = arrayOf(
                /* +z */floatArrayOf(size0 / 4, size1 / 4, size0 / 4), floatArrayOf(size0 / 4, -size1 / 4, size0 / 4), floatArrayOf(-size0 / 4, -size1 / 4, size0 / 4), floatArrayOf(-size0 / 4, size1 / 4, size0 / 4),
                /* -z */floatArrayOf(size0 / 4, size1 / 4, 0f), floatArrayOf(size0 / 4, -size1 / 4, 0f), floatArrayOf(-size0 / 4, -size1 / 4, 0f), floatArrayOf(-size0 / 4, size1 / 4, 0f))
        val cube_vertices_2_buffer = FloatBuffer.wrap(cube_vertices_2.asIterable().flatMap { it.asIterable() }.toFloatArray())
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cube_vertices_2_buffer.limit() * 4, cube_vertices_2_buffer, GLES20.GL_DYNAMIC_DRAW)
        GLES20.glEnableVertexAttribArray(pos_coord_box)
        GLES20.glVertexAttribPointer(pos_coord_box, 3, GLES20.GL_FLOAT, false, 0, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo_color_box_2)
        GLES20.glEnableVertexAttribArray(pos_color_box)
        GLES20.glVertexAttribPointer(pos_color_box, 4, GLES20.GL_UNSIGNED_BYTE, true, 0, 0)
        for (i in 0..5) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 4, GLES20.GL_UNSIGNED_SHORT, i * 4 * 2)
        }
    }
}
