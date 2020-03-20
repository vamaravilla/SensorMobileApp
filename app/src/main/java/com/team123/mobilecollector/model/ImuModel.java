package com.team123.mobilecollector.model;


import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class ImuModel {

    @SerializedName("Body")
    private final Body mBody;

    public Body getBody() {
        return mBody;
    }

    public ImuModel(Body body) {
        mBody = body;
    }

    @Override
    public String toString() {
        return "ImuModel{" +
                "mBody=" + mBody +
                '}';
    }

    public class Body {

        @SerializedName("Timestamp")
        private long timestamp;

        @SerializedName("ArrayAcc")
        private ArrayAcc mArrayAcc[];

        @SerializedName("ArrayGyro")
        private ArrayGyro mArrayGyro[];

        @SerializedName("ArrayMagn")
        private ArrayMagn mArrayMagnl[];

        public long getTimestamp() {
            return timestamp;
        }

        public ArrayAcc[] getArrayAcc() {
            return mArrayAcc;
        }

        public ArrayGyro[] getArrayGyro() {
            return mArrayGyro;
        }

        public ArrayMagn[] getArrayMagnl() {
            return mArrayMagnl;
        }

        @Override
        public String toString() {
            return "Body{" +
                    "timestamp=" + timestamp +
                    ", mArrayAcc=" + Arrays.toString(mArrayAcc) +
                    ", mArrayGyro=" + Arrays.toString(mArrayGyro) +
                    ", mArrayMagnl=" + Arrays.toString(mArrayMagnl) +
                    '}';
        }
    }

    public class ArrayAcc {

        @SerializedName("x")
        public final double x;
        @SerializedName("y")
        public final double y;
        @SerializedName("z")
        public final double z;

        public ArrayAcc(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        @Override
        public String toString() {
            return "ArrayAcc{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }

    public class ArrayGyro {

        @SerializedName("x")
        public final double x;
        @SerializedName("y")
        public final double y;
        @SerializedName("z")
        public final double z;

        public ArrayGyro(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        @Override
        public String toString() {
            return "ArrayGyro{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }

    public class ArrayMagn {

        @SerializedName("x")
        public final double x;
        @SerializedName("y")
        public final double y;
        @SerializedName("z")
        public final double z;

        public ArrayMagn(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }

        @Override
        public String toString() {
            return "ArrayMagn{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }
    }
}
