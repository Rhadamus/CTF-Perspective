package Extensions;

import Actions.CActExtension;
import Banks.CImage;
import Conditions.CCndExtension;
import Expressions.CValue;
import Objects.CObject;
import OpenGL.GLRenderer;
import RunLoop.CCreateObjectInfo;
import Runtime.MMFRuntime;
import Runtime.SurfaceView;
import Services.CBinaryFile;
import Services.CServices;
import Sprites.CRSpr;

import android.graphics.Bitmap;

public class CRunPerspective extends CRunExtension {
    public static final int ACTSETZOOMVALUE = 0;
    public static final int ACTSETPANORAMA = 1;
    public static final int ACTSETPERSPECTIVE = 2;
    public static final int ACTSETSINEWAVE = 3;
    public static final int ACTSETCUSTOM = 4;
    public static final int ACTSETNUMWAVES = 5;
    public static final int ACTSETOFFSET = 6;
    public static final int ACTSETHORIZONTAL = 7;
    public static final int ACTSETVERTICAL = 8;
    public static final int ACTSETLEFTTOP = 9;
    public static final int ACTSETRIGHTBOTTOM = 10;
    public static final int ACTSETCUSTOMVALUE = 11;
    public static final int ACTSETWIDTH = 12;
    public static final int ACTSETHEIGHT = 13;
    public static final int ACTSETRESAMPLEON = 14;
    public static final int ACTSETRESAMPLEOFF = 15;
    public static final int ACTSETSINEOFFSET = 16;
    public static final int ACTSETCUSTOMOFFSET = 17;

    public static final int EXPGETZOOMVALUE = 0;
    public static final int EXPGETOFFSET = 1;
    public static final int EXPNUMWAVES = 2;
    public static final int EXPGETCUSTOM = 3;
    public static final int EXPGETWIDTH = 4;
    public static final int EXPGETHEIGHT = 5;

    public static final int PANORAMA = 0;
    public static final int PERSPECTIVE = 1;
    public static final int SINEWAVE = 2;
    public static final int SINEOFFSET = 3;
    //public static final int CUSTOM = 4;
    //public static final int CUSTOMOFFSET = 5;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static final int LEFTBOTTOM = 0;
    public static final int RIGHTTOP = 1;

    private int[] CustomArray;
    private int Direction;
    private int Effect;
    private int Offset;
    private int PerspectiveDir;
    private int SineWaveWaves;
    private int ZoomValue;
    private boolean bRemoveShader;
    public CValue expRet = new CValue(0);
    private CImage imageTexture;
    private int indexShader;
    private boolean oldResample;
    private boolean onceOffs;
    private boolean oncePano;
    private boolean oncePers;
    private boolean onceSine;
    private boolean paused;
    private boolean resample;

    @Override // Extensions.CRunExtension
    public int getNumberOfConditions() {
        return 0;
    }

    @Override // Extensions.CRunExtension
    public int handleRunObject() {
        return 0;
    }

    /* access modifiers changed from: package-private */
    public class CImageTexture extends CImage {
        public CImageTexture(int i, int i2) {
            Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
            allocNative2((MMFRuntime.inst.app.hdr2Options & 4096) != 0, (short)-1, CServices.getBitmapPixels(createBitmap), 0, 0, 0, 0, createBitmap.getWidth(), createBitmap.getHeight(), SurfaceView.ES);
        }

        @Override // Banks.CImage
        public void onDestroy() {
            CRunPerspective.this.imageTexture = null;
        }
    }

    @Override // Extensions.CRunExtension
    public boolean createRunObject(CBinaryFile cBinaryFile, CCreateObjectInfo cCreateObjectInfo, int i) {
        cBinaryFile.skipBytes(4);
        short readShort = cBinaryFile.readShort();
        short readShort2 = cBinaryFile.readShort();
        this.ho.setX(cCreateObjectInfo.cobX);
        this.ho.setY(cCreateObjectInfo.cobY);
        this.ho.setWidth(readShort);
        this.ho.setHeight(readShort2);
        this.Effect = cBinaryFile.readByte();
        this.Direction = cBinaryFile.readByte() != 0 ? VERTICAL : HORIZONTAL;
        cBinaryFile.skipBytes(2);
        this.ZoomValue = cBinaryFile.readInt();
        this.Offset = cBinaryFile.readInt();
        this.SineWaveWaves = cBinaryFile.readInt();
        this.PerspectiveDir = cBinaryFile.readByte() != 0 ? LEFTBOTTOM : RIGHTTOP;
        this.resample = cBinaryFile.readByte() != 0;
        this.ho.roc.rcChanged = true;
        this.oldResample = (MMFRuntime.inst.app.hdr2Options & 4096) != 0;
        int i2 = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        this.CustomArray = new int[i2];
        for (int i3 = 0; i3 < i2; i3++) {
            this.CustomArray[i3] = this.ZoomValue;
        }
        this.imageTexture = new CImageTexture(this.ho.hoImgWidth, this.ho.hoImgHeight);
        this.paused = false;
        this.indexShader = -1;
        this.oncePers = false;
        this.oncePano = false;
        this.onceSine = false;
        this.onceOffs = false;
        this.bRemoveShader = false;
        return true;
    }

    @Override // Extensions.CRunExtension
    public void pauseRunObject() {
        this.paused = true;
        this.imageTexture.destroy();
    }

    @Override // Extensions.CRunExtension
    public void continueRunObject() {
        this.paused = false;
        this.imageTexture = new CImageTexture(this.ho.hoImgWidth, this.ho.hoImgHeight);
    }

    @Override // Extensions.CRunExtension
    public void destroyRunObject(boolean z) {
        this.CustomArray = null;
        CImage cImage = this.imageTexture;
        if (cImage != null) {
            cImage.destroy();
        }
        if (this.indexShader > 0 && GLRenderer.inst != null) {
            synchronized (GLRenderer.inst) {
                GLRenderer.inst.removeShader(this.indexShader);
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v10, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x023b A[Catch:{ ClassNotFoundException | IllegalAccessException | NoSuchFieldException -> 0x02ba }] */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x02f0 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x02f1  */
    @Override // Extensions.CRunExtension
    public void displayRunObject() {
        if (paused || ho.hoImgWidth == 0 || ho.hoImgHeight == 0) {
            return;
        }

        if (bRemoveShader && indexShader != -1) {
            bRemoveShader = false;
            GLRenderer.inst.removeShader(indexShader);
            indexShader = -1;
            oncePers = false;
            oncePano = false;
            onceSine = false;
            onceOffs = false;
        }

        if (indexShader == -1) {
            switch (Effect) {
                case PANORAMA:
                    indexShader = GLRenderer.inst.addShaderFromFile("panorama_ext", new String[]{"fB", "scale", "offset", "pDir"}, true, false);
                    break;
                case PERSPECTIVE:
                    indexShader = GLRenderer.inst.addShaderFromFile("perspective_ext", new String[]{"fA", "fB", "scale", "offset", "pDir"}, true, false);
                    break;
                case SINEWAVE:
                    indexShader = GLRenderer.inst.addShaderFromFile("sinewave_ext", new String[]{"Zoom", "WaveIncrement", "Offset", "scale", "offset", "pDir"}, true, false);
                    break;
                case SINEOFFSET:
                    indexShader = GLRenderer.inst.addShaderFromFile("sineoffset_ext", new String[]{"Zoom", "WaveIncrement", "Offset", "scale", "offset", "pDir"}, true, false);
                    break;
            }
        }

        if ((ho.ros.rsFlags & CRSpr.RSFLAG_VISIBLE) == 0) {
            return;
        }

        CImage bgImage = null;

        int objX = ho.hoX - rh.rhWindowX;
        int objY = ho.hoY - rh.rhWindowY;
        int objWidth = ho.hoImgWidth;
        int objHeight = ho.hoImgHeight;

        float offsetX = 0.0f;
        float offsetY = 0.0f;
        float scaleY = 1.0f;
        float scaleX = 1.0f;

        //float WaveIncrement = (float)(SineWaveWaves * 360) / (float)objHeight;

        GLRenderer.inst.readFrameToTexture(imageTexture, objX, objY, objWidth, objHeight);
        bgImage = imageTexture;

        if (bgImage == null) {
            return;
        }

        synchronized (bgImage) { synchronized (GLRenderer.inst) {
            GLRenderer.inst.pushClip(objX, objY, objWidth, objHeight);
            GLRenderer.inst.setEffectShader(indexShader);

            if (Effect == PANORAMA) {
                if (!oncePano && Direction == HORIZONTAL) {
                    //double sin = ((double) objHeight + (Math.sin(0.0d) * (double) ZoomValue)) - (double) ZoomValue;
                    //GLRenderer.inst.updateVariable1f("fB", ((float) Math.max(1.0d, sin)) / ((float) Math.max(1.0d, ((double) objHeight + (Math.sin(1.5707963267948966d) * (double) ZoomValue)) - (double) ZoomValue)));

                    GLRenderer.inst.updateVariable1f("fB", Math.max(1.0f, (float)(objHeight - ZoomValue)) / Math.max(1.0f, (float)objHeight));
                    GLRenderer.inst.updateVariable1i("pDir", 0);
                    oncePano = true;
                }
                else if (!oncePano && Direction == VERTICAL) {
                    //GLRenderer.inst.updateVariable1f("fB", ((float) Math.max(1.0, ((Math.sin(0.0d) * (double) ZoomValue) + (double) objWidth) - (double) ZoomValue)) / ((float) Math.max(1.0, ((double) objWidth + (Math.sin(1.5707963267948966d) * (double) ZoomValue)) - (double) ZoomValue)));

                    GLRenderer.inst.updateVariable1f("fB", Math.max(1.0f, (float)(objWidth - ZoomValue)) / Math.max(1.0f, (float)objWidth));
                    GLRenderer.inst.updateVariable1i("pDir", 1);
                    oncePano = true;
                }
            }
            else if (Effect == PERSPECTIVE) {
                if (!oncePers) {
                    double[] slope;

                    if (PerspectiveDir == LEFTBOTTOM) {
                        slope = LeftBottonSlope(ZoomValue, objWidth, objHeight, Direction);
                    } else {
                        slope = RightTopSlope(ZoomValue, objWidth, objHeight, Direction);
                    }

                    if (Direction == HORIZONTAL) {
                        GLRenderer.inst.updateVariable1f("fA", (float) slope[1]);
                        GLRenderer.inst.updateVariable1f("fB", (float) slope[0]);
                    } else {
                        GLRenderer.inst.updateVariable1f("fA", (float) slope[0]);
                        GLRenderer.inst.updateVariable1f("fB", (float) slope[1]);
                    }

                    GLRenderer.inst.updateVariable1i("pDir", Direction == HORIZONTAL ? 0 : 1);
                    oncePers = true;
                }
            }
            else if (Effect == SINEWAVE) {
                if (!onceSine && Direction == HORIZONTAL) {
                    GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / (float) objHeight);
                    //GLRenderer.inst.updateVariable1f("WaveIncrement", WaveIncrement * (float) objWidth);
                    GLRenderer.inst.updateVariable1f("WaveIncrement", ((float) (SineWaveWaves * 360) / (float) objHeight) * (float) objWidth);
                    GLRenderer.inst.updateVariable1f("Offset", (float) Offset);
                    GLRenderer.inst.updateVariable1i("pDir", 0);
                    onceSine = true;
                }
                else if(!onceSine && Direction == VERTICAL) {
                    GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / ((float) objWidth));
                    //GLRenderer.inst.updateVariable1f("WaveIncrement", WaveIncrement * (float) objHeight);
                    GLRenderer.inst.updateVariable1f("WaveIncrement", (float) (SineWaveWaves * 360));
                    GLRenderer.inst.updateVariable1f("Offset", (float) Offset);
                    GLRenderer.inst.updateVariable1i("pDir", 1);

                    onceSine = true;
                }
            }
            else if (Effect == SINEOFFSET) {
                if (!onceOffs && Direction == HORIZONTAL) {
                    GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / (float) objHeight);
                    GLRenderer.inst.updateVariable1f("WaveIncrement", (float) (SineWaveWaves * 360));
                    GLRenderer.inst.updateVariable1f("Offset", (float) Offset);

                    GLRenderer.inst.updateVariable1i("pDir", 0);
                    onceOffs = true;
                }
                else if (!onceOffs && Direction == VERTICAL) {
                    GLRenderer.inst.updateVariable1f("Zoom", (float) ZoomValue / (float) objHeight);
                    GLRenderer.inst.updateVariable1f("WaveIncrement", ((float) (SineWaveWaves * 360) / (float) objHeight) * (float) objWidth);
                    GLRenderer.inst.updateVariable1f("Offset", (float) Offset);

                    GLRenderer.inst.updateVariable1i("pDir", 1);
                    onceOffs = true;
                }
            }

            GLRenderer.inst.updateVariable2f("scale", scaleX, scaleY);
            GLRenderer.inst.updateVariable2f("offset", offsetX, offsetY);
            bgImage.setResampling(resample);
            GLRenderer.inst.renderImage(bgImage, objX, objY, objWidth, objHeight, 0, 0);
            GLRenderer.inst.removeEffectShader();
            GLRenderer.inst.popClip();
        } }
    }

    /* access modifiers changed from: package-private */
    public double[] LeftBottonSlope(int zoom, int width, int height, int direction) {
        /*int i5;
        double[] dArr = {0.0d, 0.0d};
        int i6 = 0;
        if (direction == 0) {
            int i7 = 0;
            i5 = 0;
            while (i7 <= width) {
                double d = (double) height;
                dArr[i5] = ((d / (((double) (((i7 * zoom) / width) + height)) / d)) + 0.5d) / d;
                i7 += width;
                i5++;
            }
        } else {
            i5 = 0;
        }
        if (direction == 1) {
            while (i6 <= height) {
                double d2 = (double) width;
                dArr[i5] = ((d2 / (((double) (((i6 * zoom) / height) + width)) / d2)) + 0.5d) / d2;
                i6 += height;
                i5++;
            }
        }
        return dArr;*/
        double[] slope = { 0.0, 0.0 };

        switch (direction) {
            case HORIZONTAL:
                slope[0] = ((double) height + 0.5) / (double) height;
                slope[1] = (((double) height / ((double) (zoom + height) / (double) height)) + 0.5) / (double) height;
                break;
            case VERTICAL:
                slope[0] = ((double) width + 0.5) / (double) width;
                slope[1] = (((double) width / ((double) (zoom + width) / (double) width)) + 0.5) / (double) width;
                break;
        }

        return slope;
    }

    /* access modifiers changed from: package-private */
    public double[] RightTopSlope(int zoom, int width, int height, int direction) {
        /*int i5;
        double[] dArr = {0.0d, 0.0d};
        int i6 = 0;
        if (direction == 0) {
            int i7 = 0;
            i5 = 0;
            while (i7 <= width) {
                double d = height;
                dArr[i5] = ((d / (((double) ((((width - i7) * zoom) / width) + height)) / d)) + 0.5d) / d;
                i7 += width;
                i5++;
            }
        } else {
            i5 = 0;
        }
        if (direction == 1) {
            while (i6 <= height) {
                double d2 = width;
                dArr[i5] = ((d2 / (((double) ((((height - i6) * zoom) / height) + width)) / d2)) + 0.5d) / d2;
                i6 += height;
                i5++;
            }
        }
        return dArr;*/
        double[] slope = { 0.0, 0.0 };

        switch (direction) {
            case HORIZONTAL:
                slope[0] = (((double) height / ((double) (zoom + height) / (double) height)) + 0.5) / (double) height;
                slope[1] = ((double) height + 0.5) / (double) height;
                break;
            case VERTICAL:
                slope[0] = (((double) width / ((double) (zoom + width) / (double) width)) + 0.5) / (double) width;
                slope[0] = ((double) width + 0.5) / (double) width;
                break;
        }

        return slope;
    }

    /* access modifiers changed from: package-private */
    public int resizePerspective(int i) {
        CImage cImage = this.imageTexture;
        if (cImage != null) {
            cImage.destroy();
        }
        this.imageTexture = new CImageTexture(this.ho.hoImgWidth, this.ho.hoImgHeight);
        int i2 = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int[] iArr = new int[i2];
        int min = Math.min(i2, i);
        for (int i3 = 0; i3 < i2; i3++) {
            if (i3 < min) {
                iArr[i3] = this.CustomArray[i3];
            } else {
                iArr[i3] = 0;
            }
        }
        this.CustomArray = iArr;
        this.ho.roc.rcChanged = true;
        return 0;
    }

    @Override // Extensions.CRunExtension
    public boolean condition(int i, CCndExtension cCndExtension) {
        return false;
    }

    @Override // Extensions.CRunExtension
    public void action(int i, CActExtension cActExtension) {
        switch (i) {
            case ACTSETZOOMVALUE:
                actSetZoomValue(cActExtension);
                break;
            case ACTSETPANORAMA:
                actSetPanorama(cActExtension);
                break;
            case ACTSETPERSPECTIVE:
                actSetPerspective(cActExtension);
                break;
            case ACTSETSINEWAVE:
                actSetSineWave(cActExtension);
                break;
            case ACTSETCUSTOM:
                actSetCustom(cActExtension);
                break;
            case ACTSETNUMWAVES:
                actSetNumWaves(cActExtension);
                break;
            case ACTSETOFFSET:
                actSetOffset(cActExtension);
                break;
            case ACTSETHORIZONTAL:
                actSetHorizontal(cActExtension);
                break;
            case ACTSETVERTICAL:
                actSetVertical(cActExtension);
                break;
            case ACTSETLEFTTOP:
                actSetLeftTop(cActExtension);
                break;
            case ACTSETRIGHTBOTTOM:
                actSetRightBottom(cActExtension);
                break;
            case ACTSETCUSTOMVALUE:
                actSetCustomValue(cActExtension);
                break;
            case ACTSETWIDTH:
                actSetWidth(cActExtension);
                break;
            case ACTSETHEIGHT:
                actSetHeight(cActExtension);
                break;
            case ACTSETRESAMPLEON:
                actSetResampleOn(cActExtension);
                break;
            case ACTSETRESAMPLEOFF:
                actSetResampleOff(cActExtension);
                break;
            case ACTSETSINEOFFSET:
                actSetSineOffset(cActExtension);
                break;
            case ACTSETCUSTOMOFFSET:
                actSetCustomOffset(cActExtension);
                break;
        }
    }

    @Override // Extensions.CRunExtension
    public CValue expression(int i) {
        switch (i) {
            case EXPGETZOOMVALUE:
                return expGetZoomValue();
            case EXPGETOFFSET:
                return expGetOffset();
            case EXPNUMWAVES:
                return expNumWaves();
            case EXPGETCUSTOM:
                return expGetCustom();
            case EXPGETWIDTH:
                return expGetWidth();
            case EXPGETHEIGHT:
                return expGetHeight();
            default:
                return null;
        }
    }

    private void actSetZoomValue(CActExtension cActExtension) {
        this.ZoomValue = cActExtension.getParamExpression(this.rh, 0);
        oncePano = false;
        oncePers = false;
        onceSine = false;
        onceOffs = false;
        this.ho.roc.rcChanged = true;
    }

    private void actSetPanorama(CActExtension cActExtension) {
        if (this.Effect != PANORAMA) {
            this.bRemoveShader = true;
        }
        this.Effect = PANORAMA;
        this.ho.roc.rcChanged = true;
    }

    private void actSetPerspective(CActExtension cActExtension) {
        if (this.Effect != PERSPECTIVE) {
            this.bRemoveShader = true;
        }
        this.Effect = PERSPECTIVE;
        this.ho.roc.rcChanged = true;
    }

    private void actSetSineWave(CActExtension cActExtension) {
        if (this.Effect != SINEWAVE) {
            this.bRemoveShader = true;
        }
        this.Effect = SINEWAVE;
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustom(CActExtension cActExtension) {
        this.ho.roc.rcChanged = true;
    }

    private void actSetNumWaves(CActExtension cActExtension) {
        this.SineWaveWaves = cActExtension.getParamExpression(this.rh, 0);
        this.onceSine = false;
        this.ho.roc.rcChanged = true;
    }

    private void actSetOffset(CActExtension cActExtension) {
        this.Offset = cActExtension.getParamExpression(this.rh, 0);
        onceSine = false;
        onceOffs = false;
        this.ho.roc.rcChanged = true;
    }

    private void actSetHorizontal(CActExtension cActExtension) {
        int i = this.Direction == HORIZONTAL ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int i2 = this.ho.hoImgWidth;
        this.Direction = HORIZONTAL;
        this.ho.roc.rcChanged = true;
        int min = Math.min(i, i2);
        int[] iArr = new int[i2];
        if (min >= 0) System.arraycopy(this.CustomArray, 0, iArr, 0, min);
        this.CustomArray = iArr;
    }

    private void actSetVertical(CActExtension cActExtension) {
        int i = this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        int i2 = this.ho.hoImgHeight;
        this.Direction = 1;
        this.ho.roc.rcChanged = true;
        int min = Math.min(i, i2);
        int[] iArr = new int[i2];
        if (min >= 0) System.arraycopy(this.CustomArray, 0, iArr, 0, min);
        this.CustomArray = iArr;
    }

    private void actSetLeftTop(CActExtension cActExtension) {
        this.PerspectiveDir = LEFTBOTTOM;
        oncePers = false;
        this.ho.roc.rcChanged = true;
    }

    private void actSetRightBottom(CActExtension cActExtension) {
        this.PerspectiveDir = RIGHTTOP;
        oncePers = false;
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustomValue(CActExtension cActExtension) {
        int paramExpression = cActExtension.getParamExpression(this.rh, 0);
        int paramExpression2 = cActExtension.getParamExpression(this.rh, 1);
        int i = this.Direction == HORIZONTAL ? this.ho.hoImgWidth : this.ho.hoImgHeight;
        if (paramExpression >= 0 && paramExpression2 < i) {
            this.CustomArray[paramExpression] = paramExpression2;
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetWidth(CActExtension cActExtension) {
        this.ho.hoImgWidth = cActExtension.getParamExpression(this.rh, 0);
        resizePerspective(this.Direction == HORIZONTAL ? this.ho.hoImgWidth : this.ho.hoImgHeight);
    }

    private void actSetHeight(CActExtension cActExtension) {
        this.ho.hoImgHeight = cActExtension.getParamExpression(this.rh, 0);
        resizePerspective(this.Direction == HORIZONTAL ? this.ho.hoImgWidth : this.ho.hoImgHeight);
    }

    private void actSetResampleOn(CActExtension cActExtension) {
        CImage cImage;
        this.resample = true;
        if (!(this.oldResample || (cImage = this.imageTexture) == null)) {
            this.oldResample = true;
            cImage.setResampling(true);
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetResampleOff(CActExtension cActExtension) {
        CImage cImage;
        this.resample = false;
        if (this.oldResample && (cImage = this.imageTexture) != null) {
            this.oldResample = false;
            cImage.setResampling(false);
        }
        this.ho.roc.rcChanged = true;
    }

    private void actSetSineOffset(CActExtension cActExtension) {
        if (this.Effect != SINEOFFSET) {
            this.bRemoveShader = true;
        }
        this.Effect = SINEOFFSET;
        this.ho.roc.rcChanged = true;
    }

    private void actSetCustomOffset(CActExtension cActExtension) {
        this.ho.roc.rcChanged = true;
    }

    private CValue expGetZoomValue() {
        this.expRet.forceInt(this.ZoomValue);
        return this.expRet;
    }

    private CValue expGetOffset() {
        this.expRet.forceInt(this.Offset);
        return this.expRet;
    }

    private CValue expNumWaves() {
        this.expRet.forceInt(this.SineWaveWaves);
        return this.expRet;
    }

    private CValue expGetCustom() {
        this.expRet.forceInt(this.CustomArray[Math.min(Math.max(0, this.ho.getExpParam().getInt()), (this.Direction == 0 ? this.ho.hoImgWidth : this.ho.hoImgHeight) - 1)]);
        return this.expRet;
    }

    private CValue expGetWidth() {
        this.expRet.forceInt(this.ho.hoImgWidth);
        return this.expRet;
    }

    private CValue expGetHeight() {
        this.expRet.forceInt(this.ho.hoImgHeight);
        return this.expRet;
    }
}
