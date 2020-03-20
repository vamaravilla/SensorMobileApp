package com.team123.mobilecollector.model;

import com.google.gson.annotations.SerializedName;

public class ThreadInfoList {

    @SerializedName("Content")
    public final ThreadInfoList.Body body;

    public ThreadInfoList(ThreadInfoList.Body body) {
        this.body = body;
    }

    public static class Body {
        @SerializedName("threads")
        public final ThreadInfo[] threads;

        public Body(ThreadInfo[] threads) {
            this.threads = threads;
        }
    }


    public static class ThreadInfo {
        @SerializedName("name")
        public final String name;

        /*
      - state
      - basePriority
      - currentPriority
      - runTimePercentage
      - runTimeTicks
      - contextSwitches
      - freeStack
      - programCounter
      - returnAddress
        */
        @SerializedName("freeStack")
        public final int freeStack;

        ThreadInfo(String name, int freeStack) {
            this.name = name;
            this.freeStack = freeStack;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (ThreadInfo ti : body.threads) {
            if (sb.length() > 0)
                sb.append("\n");

            sb.append(ti.name);
            sb.append(": ");
            sb.append(ti.freeStack);
        }

        return sb.toString();
    }
}
