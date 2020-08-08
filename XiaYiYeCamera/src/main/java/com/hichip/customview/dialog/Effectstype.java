package com.hichip.customview.dialog;


/**
 */
public enum  Effectstype {

    Slideleft(SlideLeft.class),
    Slidetop(SlideTop.class),
    SlideBottom(SlideBottom.class),
    Slideright(SlideRight.class),
    Fall(Fall.class),
    RotateBottom(RotateBottom.class),
    RotateLeft(RotateLeft.class),
    RotateRight(RotateRight.class),
    Sidefill(SideFall.class);
	
	
    private Class effectsClazz;

    private Effectstype(Class mclass) {
        effectsClazz = mclass;
    }

    public BaseEffects getAnimator() {
        try {
            return (BaseEffects) effectsClazz.newInstance();
        } catch (Exception e) {
            throw new Error("Can not init animatorClazz instance");
        }
    }
}
