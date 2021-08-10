package nz.pumbas.halpbot.objects;

import nz.pumbas.halpbot.commands.annotations.ParameterConstruction;

public class Vector3
{
    public static final Vector3 Zero = new Vector3(0, 0, 0);
    public static final Vector3 i = new Vector3(1, 0, 0);
    public static final Vector3 j = new Vector3(0, 1, 0);
    public static final Vector3 k = new Vector3(0, 0, 1);


    private final double x, y, z;

    @ParameterConstruction
    public Vector3(double x, double y) {
        this.x = x;
        this.y = y;
        this.z = 0;
    }

    @ParameterConstruction
    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public double getSquareMagnitude() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double getMagnitude() {
        return Math.sqrt(this.getSquareMagnitude());
    }

    public Vector3 divide(double divisor) {
        return new Vector3(this.x / divisor, this.y / divisor, this.z / divisor);
    }

    public Vector3 multiply(double multiplier) {
        return new Vector3(this.x * multiplier, this.y * multiplier, this.z * multiplier);
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3 getUnitVector() {
        return this.divide(this.getMagnitude());
    }

    public double dot(Vector3 other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public double getAngleBetween(Vector3 other) {
        return Math.toDegrees(Math.acos(
            this.dot(other) / (this.getMagnitude() * other.getMagnitude())
        ));
    }

    public Vector3 getParallelComponent(Vector3 other) {
        // (u.v/v.v)v
        return other.multiply(this.dot(other) / other.dot(other));
    }

    public Vector3 getPerpendicularComponent(Vector3 other) {
        return this.subtract(this.getParallelComponent(other));
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    @Override
    public String toString() {
        return String.format("Vector = [ %.2f, %.2f, %.2f ]", this.x, this.y, this.z);
    }
}