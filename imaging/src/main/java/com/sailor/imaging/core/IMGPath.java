package com.sailor.imaging.core;

import android.graphics.Color;
import android.graphics.Path;

/**
 * Created by felix on 2017/11/22 下午6:13.
 */

public class IMGPath {
    protected Path path;
    private int color = Color.WHITE;
    private float width = BASE_DOODLE_WIDTH;
    private IMGMode mode = IMGMode.DOODLE;
    public static final float BASE_DOODLE_WIDTH = 20f;

    public IMGPath() {
        this(new Path());
    }

    public IMGPath(Path path) {
        this(path, IMGMode.DOODLE);
    }

    public IMGPath(Path path, IMGMode mode) {
        this(path, mode, Color.WHITE);
    }

    public IMGPath(Path path, IMGMode mode, int color) {
        this(path, mode, color, BASE_DOODLE_WIDTH);
    }

    public IMGPath(Path path, IMGMode mode, int color, float width) {
        this.path = path;
        this.mode = mode;
        this.color = color;
        this.width = width;
        if (mode == IMGMode.MOSAIC) {
            path.setFillType(Path.FillType.EVEN_ODD);
        }
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public IMGMode getMode() {
        return mode;
    }

    public void setMode(IMGMode mode) {
        this.mode = mode;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }


}
