package com.sailor.imaging.core;

/**
 * -description:
 * -author: created by tang on 2019/11/15 9:45
 */
public enum  DoodleNumManager {
    INSTANCE;

    public DoodleNumListener mListener;


    public interface DoodleNumListener {
        void onDoodleNum(int doodleNum, int mRevertDoodleNum);
    }

}
