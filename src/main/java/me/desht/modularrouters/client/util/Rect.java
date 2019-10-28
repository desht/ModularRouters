package me.desht.modularrouters.client.util;

/**
 * Replaces AWT Rectangle
 */
public class Rect {
    public int x;
    public int y;
    public int width;
    public int height;

    public Rect(Rect other) {
        this(other.x, other.y, other.width, other.height);
    }

    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(int x, int y) {
        return x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + this.height;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
