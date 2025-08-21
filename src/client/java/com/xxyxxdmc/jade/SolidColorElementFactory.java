package com.xxyxxdmc.jade;

import com.xxyxxdmc.init.api.ISolidColorElementFactory;
import snownee.jade.api.ui.IElement;

public class SolidColorElementFactory implements ISolidColorElementFactory {
    @Override
    public IElement create(int width, int height, int color) {
        return new SolidColorElement(width, height, color);
    }
}
